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

variable "database_subnet_id" {
  description = "ID of the database subnet"
  type        = string
  default     = null
}

variable "virtual_network_id" {
  description = "ID of the virtual network for PostgreSQL Flexible Server DNS zone link"
  type        = string
  default     = null
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