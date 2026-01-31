# Create the Root Org
resource "aws_organizations_organization" "org" {
  feature_set = "ALL"
  aws_service_access_principals = ["sso.amazonaws.com"]
}

# Create a "Workloads" OU to keep things organized
resource "aws_organizations_organizational_unit" "workloads" {
  name      = "Workloads"
  parent_id = aws_organizations_organization.org.roots[0].id
}

# THE DEV ACCOUNT (Admin Playground)
resource "aws_organizations_account" "dev" {
  name      = "dev"
  email     = "gabriel.jsh+dev2@gmail.com"
  parent_id = aws_organizations_organizational_unit.workloads.id
  close_on_deletion = true
}

# THE PROD ACCOUNT (Protected Observatory)
resource "aws_organizations_account" "prod" {
  name      = "prod"
  email     = "gabriel.jsh+prod2@gmail.com"
  parent_id = aws_organizations_organizational_unit.workloads.id
  close_on_deletion = true
}