locals {
  project_name = "clarity-poc"
  environment  = "staging"
}

# Reference to root module
module "clarity_infra" {
  source = "../../"
  
  # Pass environment-specific values
  project_name = local.project_name
  environment  = local.environment
  location     = var.location
  tags         = var.tags

  # Postgres Customization
  postgres_allow_public_ip = var.postgres_allow_my_public_ip
  postgres_password = var.postgres_password
  aad_admin_object_id = var.aad_admin_object_id
  aad_admin_principal_name = var.aad_admin_principal_name
  
  # App Service Settings
  frontend_app_settings = var.frontend_app_settings
  backend_app_settings = var.backend_app_settings
} 