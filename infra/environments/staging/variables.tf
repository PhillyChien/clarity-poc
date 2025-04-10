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
