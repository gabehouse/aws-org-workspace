locals {
  instance_arn      = data.aws_ssoadmin_instances.main.arns[0]
  identity_store_id = data.aws_ssoadmin_instances.main.identity_store_ids[0]
  dev_account_id    = data.terraform_remote_state.org.outputs.dev_account_id
  prod_account_id   = data.terraform_remote_state.org.outputs.prod_account_id
}

# --- PERMISSION SETS ---

# 1. Administrator Set (for Dev Account)
resource "aws_ssoadmin_permission_set" "admin" {
  name             = "Administrator"
  instance_arn     = local.instance_arn
  session_duration = "PT8H"
}

resource "aws_ssoadmin_managed_policy_attachment" "admin_full" {
  instance_arn       = local.instance_arn
  managed_policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
  permission_set_arn = aws_ssoadmin_permission_set.admin.arn
}

# 2. SafeProdAdmin Set (for Prod Account)
resource "aws_ssoadmin_permission_set" "safe_prod" {
  name             = "SafeProdAdmin"
  instance_arn     = local.instance_arn
  session_duration = "PT4H"
}

# Power: Full Networking capabilities
resource "aws_ssoadmin_managed_policy_attachment" "vpc_manager" {
  instance_arn       = local.instance_arn
  managed_policy_arn = "arn:aws:iam::aws:policy/AmazonVPCFullAccess"
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
}

# Power: Security Eyes (See dashboards, list resources, check for hackers)
resource "aws_ssoadmin_managed_policy_attachment" "view_only" {
  instance_arn       = local.instance_arn
  managed_policy_arn = "arn:aws:iam::aws:policy/ReadOnlyAccess"
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
}

# Power: Billing (Crucial for solo engineers to monitor costs)
resource "aws_ssoadmin_managed_policy_attachment" "billing_view" {
  instance_arn       = local.instance_arn
  managed_policy_arn = "arn:aws:iam::aws:policy/job-function/Billing"
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
}

# Style & Safety Rail
resource "aws_ssoadmin_permission_set_inline_policy" "safety_and_style" {
  instance_arn       = local.instance_arn
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
  inline_policy      = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        # Fix: Using correct Actions and a broader Resource ARN for the UXC service
        Effect   = "Allow"
        Action   = [
          "uxc:GetAccountColor",
          "uxc:PutAccountColor",
          "uxc:DeleteAccountColor"
        ]
        Resource = "*" 
      }
    ]
  })
}

# --- ASSIGNMENTS ---

# Assign to DEV as ADMIN
resource "aws_ssoadmin_account_assignment" "dev_admin" {
  instance_arn       = local.instance_arn
  permission_set_arn = aws_ssoadmin_permission_set.admin.arn
  principal_id       = data.aws_identitystore_user.me.user_id
  principal_type     = "USER"
  target_id          = local.dev_account_id
  target_type        = "AWS_ACCOUNT"
}

# Assign to PROD as SAFE ADMIN
resource "aws_ssoadmin_account_assignment" "safe_prod" {
  instance_arn       = local.instance_arn
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
  principal_id       = data.aws_identitystore_user.me.user_id
  principal_type     = "USER"
  target_id          = local.prod_account_id
  target_type        = "AWS_ACCOUNT"
}
