# Infrastructure Scripts

This directory contains scripts for managing infrastructure and application configurations.

## Deploy Infrastructure

### `deploy.sh`

This script is used to deploy infrastructure in stages, separating core infrastructure and application infrastructure.

**Features**:
- Deploy Terraform infrastructure in stages (core infrastructure and application infrastructure)
- Support different environments
- Automatically execute Terraform plan and apply commands
- Support passing Terraform variables through command line parameters

**Usage**:
```bash
./deploy.sh -s STAGE -e ENVIRONMENT [-f VAR_FILE] [-v VAR=VALUE...]
```

**Parameters**:
- `-s STAGE`: Specify the deployment stage (`core` or `app`)
- `-e ENVIRONMENT`: Specify the environment name (e.g. `staging`, `production`)
- `-f VAR_FILE`: Specify the path to the Terraform variable file (optional)
- `-v VAR=VALUE`: Specify a single Terraform variable (can be used multiple times)
- `-h`: Display help information

**Examples**:
```bash
# Deploy core infrastructure to staging environment
./deploy.sh -s core -e staging

# Deploy application infrastructure to production environment
./deploy.sh -s app -e production

# Deploy core infrastructure and pass multiple variables
./deploy.sh -s core -e staging -v postgres_password=mypassword -v location=eastus

# Use variable file
./deploy.sh -s app -e production -f custom-vars.tfvars
```

**Workflow**:
1. Deploy core infrastructure (`./deploy.sh -s core -e <environment>`)
2. Generate JWT keys and store them in Key Vault (`./store_jwt_key.sh -e <environment>`)
3. Deploy application infrastructure (`./deploy.sh -s app -e <environment>`)

## RSA Key Management

### `store_jwt_key.sh`

This script is used to generate RSA key pairs and store them in Azure Key Vault. It is typically run after deploying the core infrastructure and before deploying the application.

**Features**:
- Generate RSA 2048-bit key pairs
- Generate a unique KID (Key ID)
- Store private key, public key and KID in Key Vault
- Automatically get Key Vault information from Terraform outputs or state files

**Usage**:
```bash
./store_jwt_key.sh -e environment [-k keyvault_name] [-r resource_group]
```

**Parameters**:
- `-e`: Environment name (dev, staging, production) - Required parameter
- `-k`: Azure Key Vault name (if not provided, will try to get from Terraform)
- `-r`: Resource group name (if not provided, will try to get from Terraform)
- `-h`: Display help information

**Examples**:
```bash
# Specify environment (required)
./store_jwt_key.sh -e staging

# Specify environment and Key Vault
./store_jwt_key.sh -e production -k myclarity-prod-kv

# Specify all parameters
./store_jwt_key.sh -e dev -k myclarity-dev-kv -r myclarity-dev-rg
```
