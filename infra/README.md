# Infra

## Azure Infrastructure Architecture (using Terraform)

This project uses Terraform to manage Azure cloud services, including the following components:

### Core Resources
- **Azure Container Registry (ACR)**: For storing and managing container images
- **Azure App Services (2)**: For deploying and running applications
  - App Service 1: Frontend application
  - App Service 2: Backend API service (migrated to Container App)
- **Azure PostgreSQL Flexible Server**: For data storage and management (Migrated from Single Server version due to Azure retirement plan)

### Supporting Resources
- **Virtual Network (VNet)**: For network isolation and security management
- **Azure Key Vault**: For secure storage of secrets and certificates
- **Application Insights**: For monitoring application performance and usage
- **Azure Storage Account**: For storing static assets and backups

## Project Structure

```
infra/
├── modules/               # Modules directory
│   ├── core/              # Core infrastructure module
│   │   ├── main.tf        # Core resources configuration
│   │   ├── variables.tf   # Core module variables
│   │   └── outputs.tf     # Core module outputs
│   ├── app/               # Application infrastructure module
│   │   ├── main.tf        # Application resources configuration
│   │   ├── variables.tf   # App module variables
│   │   └── outputs.tf     # App module outputs
│   ├── networking/        # Network-related configurations
│   ├── compute/           # Compute resource configurations
│   ├── container_app/     # Container App configurations
│   ├── container_registry/# Container Registry configurations
│   └── database/          # Database-related configurations
├── scripts/               # Deployment scripts
│   └── deploy.sh          # Script for deploying infrastructure in parts
└── environments/          # Environment-specific configurations
    └── staging/           # Staging environment
        ├── main.tf        # Staging-specific main configuration
        ├── variables.tf   # Staging-specific variables
        ├── outputs.tf     # Staging-specific outputs
        └── provider.tf    # Staging-specific provider config
```

## Two-Part Deployment

The infrastructure is separated into two deployment parts:

1. **Core Infrastructure** (modules/core)
   - Resource Group
   - Networking
   - Azure Container Registry
   - PostgreSQL Database
   - Log Analytics Workspace
   - Key Vault
   - Application Insights
   - Storage Account

2. **Application Infrastructure** (modules/app)
   - Frontend App Service
   - Backend Container App

## Environment Configuration

This project uses an environment-based folder structure. Currently, only a staging environment is implemented, but the structure supports additional environments (e.g., development, production) in the future. 

Resource naming convention follows the `{project_name}-{environment}-{resource_type}` format.

## Module Structure Benefits

Using modular infrastructure design provides several benefits:

1. **Separation of Concerns**: Clear distinction between core and application infrastructure
2. **Maintainability**: Easier to maintain and update specific parts of the infrastructure
3. **Reusability**: Modules can be reused across different environments
4. **Reduced Complexity**: Complex infrastructure is divided into manageable components
5. **Better Collaboration**: Different teams can work on different modules simultaneously

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
