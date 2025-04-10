resource "azurerm_service_plan" "app_plan" {
  name                = "${var.project_name}-${var.environment}-${var.app_name}-plan"
  resource_group_name = var.resource_group
  location            = var.location
  os_type             = "Linux"
  sku_name            = "F1"
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

  app_settings = {
    "WEBSITES_ENABLE_APP_SERVICE_STORAGE" = "false"
  }

  identity {
    type = "SystemAssigned"
  }
} 