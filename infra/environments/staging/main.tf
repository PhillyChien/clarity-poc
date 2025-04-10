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
} 