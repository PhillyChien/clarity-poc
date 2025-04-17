output "frontend_app_service_default_hostname" {
  description = "The default hostname of the frontend app service"
  value       = module.frontend_app_service.app_service_url
}

output "frontend_app_service_name" {
  description = "The name of the frontend app service"
  value       = module.frontend_app_service.app_service_id
}

output "backend_container_app_url" {
  description = "The URL of the backend container app"
  value       = module.backend_container_app.container_app_url
}

output "backend_container_app_name" {
  description = "The name of the backend container app"
  value       = module.backend_container_app.container_app_id
} 