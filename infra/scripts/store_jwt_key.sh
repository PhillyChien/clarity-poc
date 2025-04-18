#!/bin/bash

# Script to store JWT key pair in Azure Key Vault
# Usage: ./store_jwt_key.sh -e ENVIRONMENT [-k KEYVAULT_NAME] [-r RESOURCE_GROUP]

set -e

# Get environment variable, if not set, use command line parameter or default value
ENVIRONMENT=${ENVIRONMENT:-""}
KEYVAULT_NAME=${KEYVAULT_NAME:-""}
RESOURCE_GROUP=${RESOURCE_GROUP:-""}
PRIVATE_KEY_SECRET_NAME=${PRIVATE_KEY_SECRET_NAME:-"jwt-rsa-private-key"}
PUBLIC_KEY_SECRET_NAME=${PUBLIC_KEY_SECRET_NAME:-"jwt-rsa-public-key"}
KID_SECRET_NAME=${KID_SECRET_NAME:-"jwt-kid"}
PRIVATE_KEY_FILE="private_key.pem"
PUBLIC_KEY_FILE="public_key.pem"

# Parse command line parameters
while getopts ":k:r:e:h" opt; do
  case $opt in
    k) KEYVAULT_NAME="$OPTARG" ;;
    r) RESOURCE_GROUP="$OPTARG" ;;
    e) ENVIRONMENT="$OPTARG" ;;
    h) 
      echo "Usage: $0 [-k keyvault_name] [-r resource_group] [-e environment]"
      echo "  -k: Azure Key Vault name"
      echo "  -r: Resource group containing the Key Vault"
      echo "  -e: Environment (dev, staging, production)"
      echo "  -h: Show this help"
      exit 0
      ;;
    \?) echo "Invalid option -$OPTARG" >&2; exit 1 ;;
  esac
done

# Validate environment parameters
if [ -z "$ENVIRONMENT" ]; then
  echo "Error: Environment is required. Please provide it using -e option or ENVIRONMENT environment variable."
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_DIR="${INFRA_DIR}/environments/${ENVIRONMENT}"

# Try to get Key Vault information from Terraform outputs
if [ -z "$KEYVAULT_NAME" ] || [ -z "$RESOURCE_GROUP" ]; then
  # Try to use terraform command to get outputs (if running terraform apply in the same shell)
  if command -v terraform &> /dev/null && [ -d "$ENV_DIR" ]; then
    cd "$ENV_DIR"
    if terraform output -json &> /dev/null; then
      echo "Reading from Terraform outputs in $ENVIRONMENT environment..."
      KEYVAULT_NAME=$(terraform output -raw key_vault_name 2>/dev/null || echo "")
      RESOURCE_GROUP=$(terraform output -raw resource_group_name 2>/dev/null || echo "")
    fi
  fi
  
  # If the above method does not get the information, try to read from tfstate
  if [ -z "$KEYVAULT_NAME" ] || [ -z "$RESOURCE_GROUP" ]; then
    if [ -f "${ENV_DIR}/terraform.tfstate" ]; then
      echo "Reading from Terraform state in $ENVIRONMENT environment..."
      if command -v jq &> /dev/null; then
        KEYVAULT_NAME=$(jq -r '.outputs.key_vault_name.value // empty' "${ENV_DIR}/terraform.tfstate" 2>/dev/null || echo "")
        RESOURCE_GROUP=$(jq -r '.outputs.resource_group_name.value // empty' "${ENV_DIR}/terraform.tfstate" 2>/dev/null || echo "")
      else
        echo "Warning: jq command not found. Cannot parse Terraform state."
      fi
    else
      echo "Warning: Terraform state file not found at ${ENV_DIR}/terraform.tfstate"
    fi
  fi
fi

# Validate required parameters
if [ -z "$KEYVAULT_NAME" ]; then
  echo "Error: Key Vault name is required. Please provide it using -k option or KEYVAULT_NAME environment variable."
  exit 1
fi

if [ -z "$RESOURCE_GROUP" ]; then
  echo "Error: Resource Group is required. Please provide it using -r option or RESOURCE_GROUP environment variable."
  exit 1
fi

echo "Using Key Vault: $KEYVAULT_NAME in Resource Group: $RESOURCE_GROUP for environment: $ENVIRONMENT"

# Generate KID (using 16 bytes random hex)
KID=$(openssl rand -hex 16)
echo "Generated Key ID (KID): $KID"

echo "Generating RSA 2048 key pair..."
openssl genpkey -algorithm RSA -out $PRIVATE_KEY_FILE -pkeyopt rsa_keygen_bits:2048 || { echo "Failed to generate private key"; exit 1; }
openssl rsa -pubout -in $PRIVATE_KEY_FILE -out $PUBLIC_KEY_FILE || { echo "Failed to extract public key"; exit 1; }

echo "Storing keys in Azure Key Vault ($KEYVAULT_NAME)..."

# Check if Key Vault exists
az keyvault show --name "$KEYVAULT_NAME" --resource-group "$RESOURCE_GROUP" &>/dev/null || {
  echo "Error: Key Vault $KEYVAULT_NAME not found in resource group $RESOURCE_GROUP"
  exit 1
}

# Store private key
az keyvault secret set --vault-name "$KEYVAULT_NAME" \
                      --name "$PRIVATE_KEY_SECRET_NAME" \
                      --file "$PRIVATE_KEY_FILE" \
                      --output none || { echo "Failed to store private key in Key Vault"; exit 1; }

# Store public key
az keyvault secret set --vault-name "$KEYVAULT_NAME" \
                      --name "$PUBLIC_KEY_SECRET_NAME" \
                      --file "$PUBLIC_KEY_FILE" \
                      --output none || { echo "Failed to store public key in Key Vault"; exit 1; }

# Store KID
az keyvault secret set --vault-name "$KEYVAULT_NAME" \
                      --name "$KID_SECRET_NAME" \
                      --value "$KID" \
                      --output none || { echo "Failed to store KID in Key Vault"; exit 1; }

echo "Cleaning up local key files..."
rm $PRIVATE_KEY_FILE
rm $PUBLIC_KEY_FILE

echo "Keys successfully stored in Key Vault."

# Get Key Vault URI for application use
KEYVAULT_URI=$(az keyvault show --name "$KEYVAULT_NAME" --resource-group "$RESOURCE_GROUP" --query properties.vaultUri --output tsv)
echo "Key Vault URI: $KEYVAULT_URI"

# Output configuration reference
echo ""
echo "Application Configuration Reference:"
echo "--------------------------------"
echo "spring.cloud.azure.keyvault.secret.property-sources[0].endpoint=$KEYVAULT_URI"
echo "spring.cloud.azure.keyvault.secret.property-sources[0].credential.managed-identity-enabled=true"
echo ""
echo "JWT Configuration in Key Vault:"
echo "--------------------------------"
echo "- Private Key Secret: $PRIVATE_KEY_SECRET_NAME"
echo "- Public Key Secret: $PUBLIC_KEY_SECRET_NAME"
echo "- KID Secret: $KID_SECRET_NAME"
echo ""
echo "Please ensure your application has a managed identity with 'Key Vault Secrets User' permissions."
