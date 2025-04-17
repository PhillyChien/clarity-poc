#!/bin/bash

# Script to deploy infrastructure in separate parts
# Usage: ./deploy.sh [core|app] [staging]

set -e

STAGE=$1
ENVIRONMENT=$2

if [ -z "$STAGE" ] || [ -z "$ENVIRONMENT" ]; then
  echo "Usage: ./deploy.sh [core|app] [staging]"
  exit 1
fi

# Change directory to the staging environment directory
cd "$(dirname "$0")/../environments/$ENVIRONMENT"

# Function to deploy core infrastructure
deploy_core() {
  echo "Deploying Core Infrastructure..."
  
  terraform init
  
  terraform plan \
    -target=module.core \
    -out=core.tfplan
  
  terraform apply core.tfplan
  
  echo "Core infrastructure deployment completed!"
}

# Function to deploy application infrastructure
deploy_app() {
  echo "Deploying Application Infrastructure..."
  
  terraform plan \
    -target=module.app \
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
    echo "Invalid stage specified. Usage: ./deploy.sh [core|app] [staging]"
    exit 1
    ;;
esac 