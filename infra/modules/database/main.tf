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

}

resource "azurerm_postgresql_flexible_server_database" "main" {
  name      = "${var.project_name}db"
  server_id = azurerm_postgresql_flexible_server.main.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_private_dns_zone" "main" {
  name                = "${var.project_name}-${var.environment}.postgres.database.azure.com"
  resource_group_name = var.resource_group
}

resource "azurerm_private_dns_zone_virtual_network_link" "main" {
  name                  = "${var.project_name}-${var.environment}-dns-link"
  private_dns_zone_name = azurerm_private_dns_zone.main.name
  resource_group_name   = var.resource_group
  virtual_network_id    = var.virtual_network_id
  registration_enabled  = false
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_local_ip" {
  name             = "AllowLocalIP"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = var.postgres_allow_public_ip
  end_ip_address   = var.postgres_allow_public_ip
}