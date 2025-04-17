variable "location" {
  description = "Azure region where resources will be deployed"
  type        = string
  default     = "canadacentral"
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Project     = "clarity-poc"
    Environment = "staging"
    ManagedBy   = "terraform"
  }
} 

variable "postgres_password" {
  description = "The administrator password for the PostgreSQL Flexible Server"
  type        = string
  sensitive   = true
}

variable "postgres_allow_my_public_ip" {
  description = "Your local public IP address to allow access to the database"
  type        = string
}

# App Service Environment Settings
variable "backend_app_settings" {
  description = "Environment settings for the backend App Service"
  type        = map(string)
  default     = {
    "JWT_EXPIRATION_MS" = "86400000"
    "JWT_KID"           = "646b2b4576e3e06abfcee95c8e7d19f2"

    "SPRING_JPA_HIBERNATE_DDL_AUTO"     = "validate"

    "WEBSITES_PORT"         = "8080"
    "CORS_ALLOWED_ORIGINS" = "https://clarity-poc-staging-frontend.azurewebsites.net"
  }
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
