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

Use the following command to grant the necessary permissions to the role

```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO "<roleName>";
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