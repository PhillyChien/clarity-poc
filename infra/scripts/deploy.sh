#!/bin/bash

# Script to deploy infrastructure in separate parts
# Usage: ./deploy.sh -s STAGE -e ENVIRONMENT [-f VAR_FILE] [-v VAR=VALUE...]

set -e

# Initialize variables
STAGE=""
ENVIRONMENT=""
VAR_FILE=""
TF_VARS=()

# Parse command line arguments
while getopts ":s:e:f:v:h" opt; do
  case ${opt} in
    s )
      STAGE=$OPTARG
      ;;
    e )
      ENVIRONMENT=$OPTARG
      ;;
    f )
      VAR_FILE=$OPTARG
      ;;
    v )
      TF_VARS+=("-var" "$OPTARG")
      ;;
    h )
      echo "Usage: ./deploy.sh -s STAGE -e ENVIRONMENT [-f VAR_FILE] [-v VAR=VALUE...]"
      echo ""
      echo "Options:"
      echo "  -s STAGE           Specify the deployment stage (core or app)"
      echo "  -e ENVIRONMENT     Specify the environment (e.g., staging, production)"
      echo "  -f VAR_FILE        Specify a Terraform variable file (optional)"
      echo "  -v VAR=VALUE       Specify individual Terraform variables (can be used multiple times)"
      echo "  -h                 Display this help message"
      exit 0
      ;;
    \? )
      echo "Invalid option: $OPTARG" 1>&2
      echo "Use -h for help"
      exit 1
      ;;
    : )
      echo "Invalid option: $OPTARG requires an argument" 1>&2
      echo "Use -h for help"
      exit 1
      ;;
  esac
done

# Validate required parameters
if [ -z "$STAGE" ] || [ -z "$ENVIRONMENT" ]; then
  echo "Error: Missing required parameters."
  echo "Usage: ./deploy.sh -s STAGE -e ENVIRONMENT [-f VAR_FILE] [-v VAR=VALUE...]"
  echo "Use -h for help"
  exit 1
fi

# Change directory to the environment directory
cd "$(dirname "$0")/../environments/$ENVIRONMENT"

# Build var file arguments
VAR_FILE_ARGS=()
if [ -n "$VAR_FILE" ]; then
  VAR_FILE_ARGS+=("-var-file=$VAR_FILE")
fi

# Function to deploy core infrastructure
deploy_core() {
  echo "Deploying Core Infrastructure..."
  
  terraform init
  
  terraform plan \
    -target=module.core \
    ${VAR_FILE_ARGS[@]} \
    ${TF_VARS[@]} \
    -out=core.tfplan
  
  terraform apply core.tfplan
  
  echo "Core infrastructure deployment completed!"
}

# Function to deploy application infrastructure
deploy_app() {
  echo "Deploying Application Infrastructure..."
  
  terraform plan \
    -target=module.app \
    ${VAR_FILE_ARGS[@]} \
    ${TF_VARS[@]} \
    -out=app.tfplan
  
  terraform apply app.tfplan
  
  echo "Application infrastructure deployment completed!"
}

# Execute based on the specified stage
case $STAGE in
  core)
    deploy_core
    ;;
  app)
    deploy_app
    ;;
  *)
    echo "Invalid stage specified. Valid stages are: core, app"
    echo "Use -h for help"
    exit 1
    ;;
esac 