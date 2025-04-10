output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "container_registry_login_server" {
  description = "The URL that can be used to log into the container registry"
  value       = module.container_registry.login_server
  sensitive   = false
}

output "frontend_app_service_url" {
  description = "URL of the frontend app service"
  value       = module.frontend_app_service.app_service_url
}

output "backend_app_service_url" {
  description = "URL of the backend app service"
  value       = module.backend_app_service.app_service_url
}

output "postgresql_server_fqdn" {
  description = "The fully qualified domain name of the PostgreSQL server"
  value       = module.postgresql.server_fqdn
}

output "postgresql_admin_password" {
  description = "The administrator password for the PostgreSQL server"
  value       = module.postgresql.administrator_password
  sensitive   = true
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = azurerm_key_vault.main.vault_uri
}

output "storage_account_name" {
  description = "The name of the storage account"
  value       = azurerm_storage_account.main.name
} 