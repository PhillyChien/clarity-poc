output "app_service_id" {
  description = "ID of the app service"
  value       = azurerm_linux_web_app.app.id
}

output "app_service_url" {
  description = "URL of the app service"
  value       = "https://${azurerm_linux_web_app.app.default_hostname}"
}

output "app_service_principal_id" {
  description = "Principal ID of the app service managed identity"
  value       = azurerm_linux_web_app.app.identity[0].principal_id
}
