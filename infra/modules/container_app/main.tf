resource "azurerm_container_app_environment" "main" {
  name                       = "${var.project_name}-${var.environment}-env"
  location                   = var.location
  resource_group_name        = var.resource_group
  log_analytics_workspace_id = var.log_analytics_workspace_id

  infrastructure_subnet_id   = var.infrastructure_subnet_id
}

resource "azurerm_container_app" "app" {
  name                         = "${var.project_name}-${var.environment}-${var.app_name}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = var.resource_group
  revision_mode                = "Single"

  identity {
    type = "SystemAssigned"
  }

  registry {
    server   = replace(var.container_registry_url, "https://", "")
    username = var.container_registry_username
    password_secret_name = "registry-password"
  }

  secret {
    name  = "registry-password"
    value = var.container_registry_password
  }

  template {
    container {
      name   = var.app_name
      image  = "${replace(var.container_registry_url, "https://", "")}/${var.app_name}:${var.image_tag}"
      cpu    = var.cpu
      memory = var.memory

      # 配置環境變數
      dynamic "env" {
        for_each = var.app_settings
        content {
          name  = env.key
          value = env.value
        }
      }
    }
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas
  }

  ingress {
    external_enabled = var.external_enabled
    target_port      = var.target_port
    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }
}

# 為 PostgreSQL 分配 Contributor 角色（擁有完整存取權限）
resource "azurerm_role_assignment" "postgres_identity" {
  count                = var.enable_postgresql_identity ? 1 : 0
  scope                = var.postgresql_server_id
  role_definition_name = "Contributor"
  principal_id         = azurerm_container_app.app.identity[0].principal_id
}
