locals {
  config = yamldecode(file("${path.module}/../../../config.yaml"))
}

output "accounts" {
  description = "A map of AWS Account IDs for the organization"
  value       = local.config.accounts
}

output "region" {
  value = local.config.default_region
}

output "backend" {
  value = local.config.backend
}

output "dev_execution_role_name" {
  value = local.config.iam.dev_execution_role_name
}

output "prod_execution_role_name" {
  value = local.config.iam.dev_execution_role_name
}

output "github_gateway_role_name" {
  value = local.config.iam.gateway_role_name
}
