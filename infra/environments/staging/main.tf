locals {
  project_name = "clarity-poc"
  environment  = "staging"
}

# Get current Azure client configuration
data "azurerm_client_config" "current" {}

# Core Infrastructure Module (First Part)
module "core" {
  source = "../../modules/core"
  
  project_name            = local.project_name
  environment             = local.environment
  location                = var.location
  tags                    = var.tags
  postgres_password       = var.postgres_password
  postgres_allow_public_ip = var.postgres_allow_my_public_ip
  aad_admin_object_id     = var.aad_admin_object_id
  aad_admin_principal_name = var.aad_admin_principal_name
}

# Application Infrastructure Module (Second Part)
module "app" {
  source = "../../modules/app"
  
  project_name                      = local.project_name
  environment                       = local.environment
  location                          = module.core.resource_group_location
  resource_group_name               = module.core.resource_group_name
  app_services_subnet_id            = module.core.networking_app_services_subnet_id
  container_apps_subnet_id          = module.core.networking_container_apps_subnet_id
  log_analytics_workspace_id        = module.core.log_analytics_workspace_id
  container_registry_login_server   = module.core.container_registry_login_server
  container_registry_admin_username = module.core.container_registry_admin_username
  container_registry_admin_password = module.core.container_registry_admin_password
  postgresql_server_id              = module.core.postgresql_server_id
  key_vault_id                      = module.core.key_vault_id
  frontend_app_settings             = var.frontend_app_settings
  backend_app_settings              = var.backend_app_settings
  backend_image_tag                 = var.backend_image_tag
  frontend_image_tag                = var.frontend_image_tag
} 
