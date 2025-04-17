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

variable "postgres_password" {
  description = "The administrator password for the PostgreSQL Flexible Server"
  type        = string
  sensitive   = true
}

variable "postgres_allow_public_ip" {
  description = "The public IP address to allow access to the database"
  type        = string
}

variable "aad_admin_object_id" {
  description = "The Object ID of the Azure AD user to be set as PostgreSQL administrator"
  type        = string
}

variable "aad_admin_principal_name" {
  description = "The display name/principal name of the Azure AD user to be set as PostgreSQL administrator"
  type        = string
}