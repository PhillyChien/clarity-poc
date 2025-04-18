output "resource_group_name" {
  description = "The name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "resource_group_location" {
  description = "The location of the resource group"
  value       = azurerm_resource_group.main.location
}

output "log_analytics_workspace_id" {
  description = "The ID of the Log Analytics Workspace"
  value       = azurerm_log_analytics_workspace.main.id
}

output "container_registry_login_server" {
  description = "The URL of the Container Registry"
  value       = module.container_registry.login_server
}

output "container_registry_admin_username" {
  description = "The admin username for the Container Registry"
  value       = module.container_registry.admin_username
}

output "container_registry_admin_password" {
  description = "The admin password for the Container Registry"
  value       = module.container_registry.admin_password
  sensitive   = true
}

output "postgresql_server_id" {
  description = "The ID of the PostgreSQL server"
  value       = module.postgresql.server_id
}

output "postgresql_server_fqdn" {
  description = "The fully qualified domain name of the PostgreSQL server"
  value       = module.postgresql.server_fqdn
}

output "networking_app_services_subnet_id" {
  description = "The ID of the App Services subnet"
  value       = module.networking.app_services_subnet_id
}

output "networking_container_apps_subnet_id" {
  description = "The ID of the Container Apps subnet"
  value       = module.networking.container_apps_subnet_id
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = azurerm_key_vault.main.vault_uri
}

output "key_vault_name" {
  description = "The name of the Key Vault"
  value       = azurerm_key_vault.main.name
}

output "storage_account_name" {
  description = "The name of the storage account"
  value       = azurerm_storage_account.main.name
} 