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

variable "log_analytics_workspace_id" {
  description = "Log Analytics Workspace ID for Container App Environment"
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

variable "cpu" {
  description = "CPU allocation for the container (in cores or millicores, e.g., 0.5 or 500m)"
  type        = string
  default     = "0.5"
}

variable "memory" {
  description = "Memory allocation for the container (e.g., 1Gi)"
  type        = string
  default     = "1Gi"
}

variable "min_replicas" {
  description = "Minimum number of replicas"
  type        = number
  default     = 1
}

variable "max_replicas" {
  description = "Maximum number of replicas for auto-scaling"
  type        = number
  default     = 3
}

variable "target_port" {
  description = "Container port to expose"
  type        = number
  default     = 8080
}

variable "external_enabled" {
  description = "Whether the app should be accessible from the internet"
  type        = bool
  default     = true
}

variable "app_settings" {
  description = "環境設置，用於配置 Container App"
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

variable "infrastructure_subnet_id" {
  description = "VNet 中用於 Container App 環境的基礎設施子網 ID"
  type        = string
  default     = null
}

variable "vnet_integration_enabled" {
  description = "是否啟用 VNet 整合"
  type        = bool
  default     = false
}

variable "image_tag" {
  description = "Container image tag to deploy"
  type        = string
  default     = "latest"
} 