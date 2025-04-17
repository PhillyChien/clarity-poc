output "server_id" {
  description = "ID of the PostgreSQL flexible server"
  value       = azurerm_postgresql_flexible_server.main.id
}

output "server_fqdn" {
  description = "The fully qualified domain name of the PostgreSQL flexible server"
  value       = azurerm_postgresql_flexible_server.main.fqdn
}

output "administrator_login" {
  description = "The administrator login for the PostgreSQL flexible server"
  value       = azurerm_postgresql_flexible_server.main.administrator_login
  sensitive   = true
}

output "administrator_password" {
  description = "The administrator password for the PostgreSQL flexible server"
  value       = azurerm_postgresql_flexible_server.main.administrator_password
  sensitive   = true
}

output "database_name" {
  description = "The name of the PostgreSQL database"
  value       = azurerm_postgresql_flexible_server_database.main.name
}

output "server_name" {
  description = "The name of the PostgreSQL flexible server"
  value       = azurerm_postgresql_flexible_server.main.name
} 