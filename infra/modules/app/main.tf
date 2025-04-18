# Application Infrastructure Module

locals {
  # 前端應用的環境設置
  frontend_settings = var.frontend_app_settings
  
  # 後端應用的環境設置
  backend_settings = var.backend_app_settings
}

# App Services
module "frontend_app_service" {
  source                     = "../compute"
  project_name               = var.project_name
  environment                = var.environment
  resource_group             = var.resource_group_name
  location                   = var.location
  app_name                   = "frontend"
  app_services_subnet_id     = var.app_services_subnet_id
  container_registry_url     = "https://${var.container_registry_login_server}"
  container_registry_username = var.container_registry_admin_username
  container_registry_password = var.container_registry_admin_password
  app_settings               = local.frontend_settings
  enable_postgresql_identity = false
  image_tag                  = var.frontend_image_tag
}

# 後端遷移到 Container App
module "backend_container_app" {
  source                     = "../container_app"
  project_name               = var.project_name
  environment                = var.environment
  resource_group             = var.resource_group_name
  location                   = var.location
  app_name                   = "backend"
  log_analytics_workspace_id = var.log_analytics_workspace_id
  container_registry_url     = "https://${var.container_registry_login_server}"
  container_registry_username = var.container_registry_admin_username
  container_registry_password = var.container_registry_admin_password
  app_settings               = local.backend_settings
  enable_postgresql_identity = true
  postgresql_server_id       = var.postgresql_server_id
  key_vault_id               = var.key_vault_id
  infrastructure_subnet_id   = var.container_apps_subnet_id
  vnet_integration_enabled   = true
  target_port                = 8080
  cpu                        = "1.0"
  memory                     = "2Gi"
  min_replicas               = 1
  max_replicas               = 5
  external_enabled           = true
  image_tag                  = var.backend_image_tag
} 