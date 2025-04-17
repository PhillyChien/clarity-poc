variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name (e.g., staging, production)"
  type        = string
}

variable "resource_group" {
  description = "Name of the resource group"
  type        = string
}

variable "location" {
  description = "Azure region where resources will be deployed"
  type        = string
}

variable "app_name" {
  description = "Name of the application (e.g., frontend, backend)"
  type        = string
}

variable "app_services_subnet_id" {
  description = "The subnet ID for App Service VNet Integration"
  type        = string
}

variable "container_registry_url" {
  description = "URL of the container registry"
  type        = string
}

variable "container_registry_username" {
  description = "Username for the container registry"
  type        = string
}

variable "container_registry_password" {
  description = "Password for the container registry"
  type        = string
  sensitive   = true
}

variable "app_settings" {
  description = "環境設置，用於配置 App Service"
  type        = map(string)
  default     = {}
}

variable "enable_postgresql_identity" {
  description = "是否啟用 PostgreSQL 角色分配，並設置 Managed Identity"
  type        = bool
  default     = false
}

variable "postgresql_server_id" {
  description = "PostgreSQL 服務器的資源 ID，用於角色分配"
  type        = string
  default     = ""
}

variable "image_tag" {
  description = "Container image tag to deploy"
  type        = string
  default     = "latest"
} 