# Infra

## Azure Infrastructure Architecture (using Terraform)

This project uses Terraform to manage Azure cloud services, including the following components:

### Core Resources
- **Azure Container Registry (ACR)**: For storing and managing container images
- **Azure App Services (2)**: For deploying and running applications
  - App Service 1: Frontend application
  - App Service 2: Backend API service
- **Azure PostgreSQL Flexible Server**: For data storage and management (Migrated from Single Server version due to Azure retirement plan)

### Supporting Resources
- **Virtual Network (VNet)**: For network isolation and security management
- **Azure Key Vault**: For secure storage of secrets and certificates
- **Application Insights**: For monitoring application performance and usage
- **Azure Storage Account**: For storing static assets and backups

## Project Structure

```
infra/
├── main.tf                # Main root module configuration
├── variables.tf           # Root module variable definitions
├── outputs.tf             # Root module output definitions
├── provider.tf            # Provider configuration (shared)
├── modules/               # Modules directory
│   ├── networking/        # Network-related configurations
│   ├── compute/           # Compute resource configurations
│   └── database/          # Database-related configurations
└── environments/          # Environment-specific configurations
    └── staging/           # Staging environment
        ├── main.tf        # Staging-specific main configuration
        ├── variables.tf   # Staging-specific variables
        ├── outputs.tf     # Staging-specific outputs
        └── provider.tf    # Staging-specific provider config
```

## Environment Configuration

This project uses an environment-based folder structure. Currently, only a staging environment is implemented, but the structure supports additional environments (e.g., development, production) in the future. 

Resource naming convention follows the `{project_name}-{environment}-{resource_type}` format.

## Deployment Steps

1. Install Terraform (v1.11.3)
2. Configure Azure credentials (Azure CLI or Service Principal)
3. Navigate to the appropriate environment directory:
   ```
   cd infra/environments/staging
   ```
4. Initialize Terraform:
   ```
   terraform init
   ```
5. Plan deployment:
   ```
   terraform plan -out=tfplan
   ```
6. Apply deployment:
   ```
   terraform apply tfplan
   ```

## PostgreSQL Migration: Single Server to Flexible Server

As of February 2025, Azure announced the retirement of PostgreSQL Single Server, scheduled for March 28, 2025. In response, this infrastructure has been updated to use PostgreSQL Flexible Server, which offers:

- Improved performance and better price options
- Zone-redundant high availability
- Configurable maintenance windows
- More control over database configuration

### Migration Impact

- Connection strings need to be updated to the new server endpoint
- New server naming convention: `{project_name}-{environment}-psql-flex`
- Database collation setting updated to `en_US.utf8`
- Network connectivity managed through Private DNS zones

## Notes

- All secrets should be stored in Azure Key Vault
- Network Security Groups should be properly configured to restrict access
- Terraform remote state storage is configured but commented out; uncomment and configure the backend section in provider.tf when ready to use
