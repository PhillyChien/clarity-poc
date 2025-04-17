## How to deploy the infrastructure on Azure from scratch

This Document is for how to deploy the infrastructure on Azure from scratch and also how to continue apply the changes to the existing infrastructure.


Deploy the first time:

1. 設定 Azure 訂用帳戶
2. 登入 AZ CLI
3. Check variables in the terraform.tfvars file
4. Terraform Apply 
5. Github Actions 設定 ACR: REGISTRY_LOGIN_SERVER, REGISTRY_USERNAME, REGISTRY_PASSWORD
6. Create AAD Role for Container App on PostgreSQL Flexible Server
7. Terraform Apply
