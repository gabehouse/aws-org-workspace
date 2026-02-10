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

output "sso_admin_user" {
  description = "The username created manually in the AWS SSO Console"
  value       = local.config.sso_admin_user
}
