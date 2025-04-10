# Main Terraform configuration

locals {
  resource_prefix = "${var.project_name}-${var.environment}"
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "${local.resource_prefix}-rg"
  location = var.location
  tags     = var.tags
}

# Networking
module "networking" {
  source          = "./modules/networking"
  project_name    = var.project_name
  environment     = var.environment
  resource_group  = azurerm_resource_group.main.name
  location        = azurerm_resource_group.main.location
}

# Azure Container Registry
module "container_registry" {
  source          = "./modules/container_registry"
  project_name    = var.project_name
  environment     = var.environment
  resource_group  = azurerm_resource_group.main.name
  location        = azurerm_resource_group.main.location
}

# App Services
module "frontend_app_service" {
  source                     = "./modules/compute"
  project_name               = var.project_name
  environment                = var.environment
  resource_group             = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  app_name                   = "frontend"
  container_registry_url     = "https://${module.container_registry.login_server}"
  container_registry_username = module.container_registry.admin_username
  container_registry_password = module.container_registry.admin_password
}

module "backend_app_service" {
  source                     = "./modules/compute"
  project_name               = var.project_name
  environment                = var.environment
  resource_group             = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  app_name                   = "backend"
  container_registry_url     = "https://${module.container_registry.login_server}"
  container_registry_username = module.container_registry.admin_username
  container_registry_password = module.container_registry.admin_password
}

# Database
module "postgresql" {
  source             = "./modules/database"
  project_name       = var.project_name
  environment        = var.environment
  resource_group     = azurerm_resource_group.main.name
  location           = azurerm_resource_group.main.location
  database_subnet_id = module.networking.database_subnet_id
  virtual_network_id = module.networking.vnet_id
  postgres_password  = var.postgres_password
  postgres_allow_public_ip = var.postgres_allow_public_ip
}

# Key Vault
resource "azurerm_key_vault" "main" {
  name                = "${local.resource_prefix}-kv"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
  tags                = var.tags
}

# Log Analytics Workspace
resource "azurerm_log_analytics_workspace" "main" {
  name                = "${local.resource_prefix}-log"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = var.tags
}

# Application Insights
resource "azurerm_application_insights" "main" {
  name                = "${local.resource_prefix}-appinsights"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  application_type    = "web"
  tags                = var.tags
  workspace_id        = azurerm_log_analytics_workspace.main.id
}

# Storage Account
resource "azurerm_storage_account" "main" {
  name                     = "${replace(var.project_name, "-", "")}${var.environment}sa"
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  tags                     = var.tags
}

# Get current Azure client configuration
data "azurerm_client_config" "current" {} 