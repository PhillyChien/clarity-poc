output "resource_group_name" {
  description = "Name of the resource group"
  value       = module.core.resource_group_name
}

output "container_registry_login_server" {
  description = "The URL that can be used to log into the container registry"
  value       = module.core.container_registry_login_server
}

output "frontend_app_service_url" {
  description = "URL of the frontend app service"
  value       = "https://${module.app.frontend_app_service_default_hostname}"
}

output "backend_container_app_url" {
  description = "URL of the backend container app"
  value       = module.app.backend_container_app_url
}

output "postgresql_server_fqdn" {
  description = "The fully qualified domain name of the PostgreSQL server"
  value       = module.core.postgresql_server_fqdn
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = module.core.key_vault_uri
}

output "key_vault_name" {
  description = "The name of the Key Vault"
  value       = module.core.key_vault_name
}

output "storage_account_name" {
  description = "The name of the storage account"
  value       = module.core.storage_account_name
} 