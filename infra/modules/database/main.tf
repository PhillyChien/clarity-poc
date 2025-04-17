resource "azurerm_postgresql_flexible_server" "main" {
  name                = "${var.project_name}-${var.environment}-psql-flex"
  location            = var.location
  resource_group_name = var.resource_group
  zone                = "1"

  sku_name   = "B_Standard_B1ms"
  storage_mb = 32768
  version    = "11"

  administrator_login    = "psqladmin"
  administrator_password = var.postgres_password

  public_network_access_enabled = true

  backup_retention_days        = 7
  geo_redundant_backup_enabled = false
  
  maintenance_window {
    day_of_week  = 0  # Monday
    start_hour   = 2  # 2:00 AM
    start_minute = 0
  }

  # Enable Azure AD authentication
  authentication {
    active_directory_auth_enabled = true
    password_auth_enabled         = true
    tenant_id                     = data.azurerm_client_config.current.tenant_id
  }

}

data "azurerm_client_config" "current" {}

resource "azurerm_postgresql_flexible_server_database" "main" {
  name      = "${var.project_name}db"
  server_id = azurerm_postgresql_flexible_server.main.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_local_ip" {
  name             = "AllowLocalIP"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = var.postgres_allow_public_ip
  end_ip_address   = var.postgres_allow_public_ip
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "admin" {
  server_name         = azurerm_postgresql_flexible_server.main.name
  resource_group_name = var.resource_group
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = var.aad_admin_object_id
  principal_name      = var.aad_admin_principal_name
  principal_type      = "User"
}