# Infra

## Azure Infrastructure Architecture (using Terraform)

This project uses Terraform to manage Azure cloud services, including the following components:

### Core Resources
- **Azure Container Registry (ACR)**: For storing and managing container images
- **Azure App Services (2)**: For deploying and running applications
  - App Service 1: Frontend application
  - App Service 2: Backend API service
- **Azure PostgreSQL**: For data storage and management

### Supporting Resources
- **Virtual Network (VNet)**: For network isolation and security management
- **Azure Key Vault**: For secure storage of secrets and certificates
- **Application Insights**: For monitoring application performance and usage
- **Azure Storage Account**: For storing static assets and backups

## Project Structure

```
infra/
├── main.tf          # Main configuration file
├── variables.tf     # Variable definitions
├── outputs.tf       # Output definitions
├── provider.tf      # Provider configuration
└── modules/         # Modules directory
    ├── networking/  # Network-related configurations
    ├── compute/     # Compute resource configurations
    └── database/    # Database-related configurations
```

## Environment Configuration

This project maintains only a staging environment, with all resources deployed to this environment. Resource naming convention will follow the `{project_name}-staging-{resource_type}` format.

## Deployment Steps

1. Install Terraform (v1.11.3)
2. Configure Azure credentials (Azure CLI or Service Principal)
3. Initialize Terraform: `terraform init`
4. Plan deployment: `terraform plan -out=tfplan`
5. Apply deployment: `terraform apply tfplan`

## Notes

- All secrets should be stored in Azure Key Vault
- Network Security Groups should be properly configured to restrict access
- Consider using Terraform remote state storage (Azure Storage)
