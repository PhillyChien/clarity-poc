output "resource_group_name" {
  description = "Name of the resource group"
  value       = module.clarity_infra.resource_group_name
}

output "container_registry_login_server" {
  description = "The URL that can be used to log into the container registry"
  value       = module.clarity_infra.container_registry_login_server
}

output "frontend_app_service_url" {
  description = "URL of the frontend app service"
  value       = module.clarity_infra.frontend_app_service_url
}

output "backend_container_app_url" {
  description = "URL of the backend container app"
  value       = module.clarity_infra.backend_container_app_url
}

output "postgresql_server_fqdn" {
  description = "The fully qualified domain name of the PostgreSQL server"
  value       = module.clarity_infra.postgresql_server_fqdn
}

output "postgresql_admin_password" {
  description = "The administrator password for the PostgreSQL server"
  value       = module.clarity_infra.postgresql_admin_password
  sensitive   = true
}

output "key_vault_uri" {
  description = "The URI of the Key Vault"
  value       = module.clarity_infra.key_vault_uri
}

output "storage_account_name" {
  description = "The name of the storage account"
  value       = module.clarity_infra.storage_account_name
} 