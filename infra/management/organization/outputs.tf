output "org_id" {
  description = "The ID of the AWS Organization"
  value       = aws_organizations_organization.org.id
}

output "dev_account_id" {
  description = "The Account ID for the Development environment"
  value       = aws_organizations_account.dev.id
}

output "prod_account_id" {
  description = "The Account ID for the Production environment"
  value       = aws_organizations_account.prod.id
}

output "workloads_ou_id" {
  description = "The ID of the Workloads Organizational Unit"
  value       = aws_organizations_organizational_unit.workloads.id
}