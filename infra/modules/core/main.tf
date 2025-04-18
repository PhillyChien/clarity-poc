# Core Infrastructure Module

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
  source          = "../networking"
  project_name    = var.project_name
  environment     = var.environment
  resource_group  = azurerm_resource_group.main.name
  location        = azurerm_resource_group.main.location
}

# Azure Container Registry
module "container_registry" {
  source          = "../container_registry"
  project_name    = var.project_name
  environment     = var.environment
  resource_group  = azurerm_resource_group.main.name
  location        = azurerm_resource_group.main.location
}

# Database
module "postgresql" {
  source                  = "../database"
  project_name            = var.project_name
  environment             = var.environment
  resource_group          = azurerm_resource_group.main.name
  location                = azurerm_resource_group.main.location
  postgres_password       = var.postgres_password
  postgres_allow_public_ip = var.postgres_allow_public_ip
  aad_admin_object_id     = var.aad_admin_object_id
  aad_admin_principal_name = var.aad_admin_principal_name
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

# Key Vault
resource "azurerm_key_vault" "main" {
  name                = "${local.resource_prefix}-kv"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
  tags                = var.tags

  enable_rbac_authorization = true
}

resource "azurerm_role_assignment" "terraform_executor_kv_admin" {
  scope                = azurerm_key_vault.main.id
  role_definition_name = "Key Vault Administrator"
  principal_id         = data.azurerm_client_config.current.object_id
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