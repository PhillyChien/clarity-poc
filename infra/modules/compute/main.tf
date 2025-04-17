resource "azurerm_service_plan" "app_plan" {
  name                = "${var.project_name}-${var.environment}-${var.app_name}-plan"
  resource_group_name = var.resource_group
  location            = var.location
  os_type             = "Linux"
  sku_name            = "B1"
}

resource "azurerm_linux_web_app" "app" {
  name                = "${var.project_name}-${var.environment}-${var.app_name}"
  resource_group_name = var.resource_group
  location            = var.location
  service_plan_id     = azurerm_service_plan.app_plan.id
  
  site_config {
    always_on = false
    container_registry_use_managed_identity = false
    
    application_stack {
      docker_image_name        = "${var.app_name}:latest"
      docker_registry_url      = var.container_registry_url
      docker_registry_username = var.container_registry_username
      docker_registry_password = var.container_registry_password
    }
  }

  app_settings = var.app_settings

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_app_service_virtual_network_swift_connection" "app_vnet_integration" {
  app_service_id = azurerm_linux_web_app.app.id
  subnet_id      = var.app_services_subnet_id
}

# 根據 enable_postgresql_identity 設置來添加 PostgreSQL 角色分配
resource "azurerm_role_assignment" "postgres_identity" {
  count                = var.enable_postgresql_identity ? 1 : 0
  scope                = var.postgresql_server_id
  role_definition_name = "Contributor"
  principal_id         = azurerm_linux_web_app.app.identity[0].principal_id
} 