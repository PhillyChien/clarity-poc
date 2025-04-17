variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "clarity-poc"
}

variable "environment" {
  description = "Environment name (e.g., staging, production)"
  type        = string
}

variable "location" {
  description = "Azure region where resources will be deployed"
  type        = string
  default     = "eastus"
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    ManagedBy = "terraform"
  }
} 

variable "postgres_password" {
  description = "The administrator password for the PostgreSQL Flexible Server"
  type        = string
  sensitive   = true
}

variable "postgres_allow_public_ip" {
  description = "The public IP address to allow access to the database"
  type        = string
}

# App Service Environment Settings
variable "backend_app_settings" {
  description = "Environment settings for the backend App Service"
  type        = map(string)
  default     = {}
}

variable "frontend_app_settings" {
  description = "Environment settings for the frontend App Service"
  type        = map(string)
  default     = {}
}

variable "aad_admin_object_id" {
  description = "The Object ID of the Azure AD user to be set as PostgreSQL administrator"
  type        = string
}

variable "aad_admin_principal_name" {
  description = "The display name/principal name of the Azure AD user to be set as PostgreSQL administrator"
  type        = string
}