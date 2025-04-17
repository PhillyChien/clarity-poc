output "vnet_id" {
  description = "ID of the created virtual network"
  value       = azurerm_virtual_network.main.id
}

output "app_services_subnet_id" {
  description = "ID of the app services subnet"
  value       = azurerm_subnet.app_services.id
}

output "container_apps_subnet_id" {
  description = "ID of the container apps subnet"
  value       = azurerm_subnet.container_apps.id
}

output "network_security_group_id" {
  description = "ID of the network security group"
  value       = azurerm_network_security_group.main.id
} 