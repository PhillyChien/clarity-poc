variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., staging, production)"
  type        = string
}

variable "location" {
  description = "Azure region where resources will be deployed"
  type        = string
}

variable "resource_group_name" {
  description = "The name of the resource group"
  type        = string
}

variable "app_services_subnet_id" {
  description = "The ID of the App Services subnet"
  type        = string
}

variable "container_apps_subnet_id" {
  description = "The ID of the Container Apps subnet"
  type        = string
}

variable "log_analytics_workspace_id" {
  description = "The ID of the Log Analytics Workspace"
  type        = string
}

variable "container_registry_login_server" {
  description = "The URL of the Container Registry"
  type        = string
}

variable "container_registry_admin_username" {
  description = "The admin username for the Container Registry"
  type        = string
}

variable "container_registry_admin_password" {
  description = "The admin password for the Container Registry"
  type        = string
  sensitive   = true
}

variable "postgresql_server_id" {
  description = "The ID of the PostgreSQL server"
  type        = string
}

variable "frontend_app_settings" {
  description = "Environment settings for the frontend App Service"
  type        = map(string)
  default     = {}
}

variable "backend_app_settings" {
  description = "Environment settings for the backend App Service"
  type        = map(string)
  default     = {}
}

variable "backend_image_tag" {
  description = "Container image tag for the backend application"
  type        = string
  default     = "latest"
}

variable "frontend_image_tag" {
  description = "Container image tag for the frontend application"
  type        = string
  default     = "latest"
} 