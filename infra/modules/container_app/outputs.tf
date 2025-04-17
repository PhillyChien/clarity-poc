output "container_app_id" {
  description = "ID of the deployed Container App"
  value       = azurerm_container_app.app.id
}

output "container_app_url" {
  description = "URL of the deployed Container App"
  value       = azurerm_container_app.app.latest_revision_fqdn
}

output "principal_id" {
  description = "The principal ID of the Container App's managed identity"
  value       = azurerm_container_app.app.identity[0].principal_id
}

output "container_app_environment_id" {
  description = "ID of the Container App Environment"
  value       = azurerm_container_app_environment.main.id
} 