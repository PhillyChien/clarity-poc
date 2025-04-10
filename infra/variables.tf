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