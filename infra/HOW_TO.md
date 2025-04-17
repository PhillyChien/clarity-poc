# How to Deploy the Infrastructure in Two Parts

This guide provides detailed instructions for deploying the infrastructure in two separate parts.

## Prerequisites

1. Install Terraform (v1.11.3 or later)
2. Configure Azure credentials (Azure CLI or Service Principal)
3. Navigate to the infrastructure directory: `cd infra`

## Part 1: Deploy Core Infrastructure

This part deploys all foundational infrastructure components:

```bash
# Navigate to the staging environment directory
cd environments/staging

# Initialize Terraform
terraform init

# Plan the deployment of core infrastructure
terraform plan -target=module.core -out=core.tfplan

# Review the plan then apply it
terraform apply core.tfplan
```

## After Core Infrastructure is deployed

- Setup GitHub Actions Secrets for ACR Deployment

- Build and push Docker images to ACR through GitHub Actions (You can use workflow_dispatch to trigger the deployment)

## Part 2: Deploy Application Infrastructure

```bash
# Plan the deployment of application infrastructure
terraform plan -target=module.app -out=app.tfplan

# Review the plan then apply it
terraform apply app.tfplan
```

## After Application Infrastructure is deployed

- Setup Manged Identity for backend container app

## Verification Steps

1. After Core infrastructure deployment:
   - Verify that the resource group is created
   - Confirm database is accessible
   - Check that container registry is ready

2. After Application infrastructure deployment:
   - Check that the frontend app is running
   - Verify the backend container app is operational
   - Test the connectivity between the frontend and backend

## Using the Deployment Script

A deployment script is provided to simplify the process:

```bash
# Deploy core infrastructure
./scripts/deploy.sh core

# Deploy application infrastructure
./scripts/deploy.sh app
```

## Module Structure

The infrastructure is organized into two main modules:

1. **Core module** (`modules/core`):
   - Contains all fundamental infrastructure resources
   - Includes resource group, networking, database, etc.

2. **Application module** (`modules/app`):
   - Contains application-specific resources
   - Includes frontend and backend applications

This modular structure allows for clearer organization and easier maintenance.

## Troubleshooting

- If any service fails to deploy, check Azure portal for error details
- For database connectivity issues, verify the firewall settings
- For container app issues, check the container registry access permissions

# # Manage Azure AD (Microsoft Entra) Identities in Azure Database for PostgreSQL Flexible Server

This guide explains how to manage Microsoft Entra roles in Azure Database for PostgreSQL flexible server.

## Prerequisites

- Azure CLI
- psql
- Azure PostgreSQL Flexible Server (version 13 or above)
- Azure AD authentication enabled (default for Flexible Server)

## Setup Azure AD Administrator for PostgreSQL

The Azure AD Administrator is required to register Azure AD identities as PostgreSQL roles.

<your-rg> is`clarity-poc-staging-rg`
<your-server> is `clarity-poc-staging-psql-flex`
<your-username> is `chienaeae@gmail.com`
<your-aad-object-id> is `4b3716f9-e037-4236-b013-74b3a5d9758`

```bash
az postgres flexible-server ad-admin create \
  --resource-group <your-rg> \
  --server-name <your-server> \
  --display-name "<your-username>" \
  --object-id <your-aad-object-id>
```

You can use the following command to get your current account's object ID:

```bash
az ad signed-in-user show --query id -o tsv

```

You can use the following command to get your current account's username:

```bash
az account show --query user.name -o tsv
```


To verify the Azure AD Administrator has been created, you can use the following command:

```bash
az postgres flexible-server ad-admin list \
  --resource-group <your-rg> \
  --server-name <your-server>
```

## Use Azure AD Access Token to manage the AAD Role in PostgreSQL

Acquire an access token for the Azure Database for PostgreSQL Flexible Server resource.
```bash
export PGPASSWORD=$(az account get-access-token \
  --resource-type oss-rdbms \
  --query accessToken -o tsv)
```

Connect to the Azure Database for PostgreSQL Flexible Server using the access token.
```bash
psql "host=<your-server>.postgres.database.azure.com \
      port=5432 \
      dbname=postgres \
      user=<your-username> \
      sslmode=require"

```
- dbname uses built-in database `postgres`, not `<your-database>`

### Create an Azure Database for PostgreSQL flexible server user for your Managed Identity

Use the following command to create the Microsoft Entra non-admin user for your Managed Identity

```sql
select * from pgaadauth_create_principal('<roleName>', false, false);
```
- E.g. `select * from pg_catalog.pgaadauth_create_principal('clarity-poc-staging-backend', false, false);`

Use the following command to grant the necessary permissions to the role (Note: You should use the same user doing the migration to grant the permissions)

To grant the necessary permissions to the role, you can use the following command:
```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO "<roleName>";

GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO "<roleName>";
```

### List the existing AAD Role in PostgreSQL

```sql
-- List admin AAD users
SELECT * FROM pg_catalog.pgaadauth_list_principals(true);

-- List both admin and non-admin AAD users
SELECT * FROM pg_catalog.pgaadauth_list_principals(false);

```

### Drop the existing AAD Role in PostgreSQL

```sql
DROP ROLE "<roleName>";
```
- Be sure to replace `<identity-object-id>` (enclosed in double quotes) with the actual object ID of your managed identity.