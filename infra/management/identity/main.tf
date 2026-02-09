module "globals" {
  source = "../../modules/globals"
}

locals {
  instance_arn = data.aws_ssoadmin_instances.main.arns[0]
  # identity_store_id = data.aws_ssoadmin_instances.main.identity_store_ids[0]
  dev_account_id  = module.globals.accounts.dev
  prod_account_id = module.globals.accounts.prod
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

# The "Keep Moving" Power: Full Admin
# This removes the need for vpc_manager, iam_manager, and billing_view
resource "aws_ssoadmin_managed_policy_attachment" "full_admin" {
  instance_arn       = local.instance_arn
  managed_policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
}

resource "aws_ssoadmin_permission_set_inline_policy" "safety_rail" {
  instance_arn       = local.instance_arn
  permission_set_arn = aws_ssoadmin_permission_set.safe_prod.arn
  inline_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        # DENY deletion of the State Bucket
        # This protects you from yourself!
        Sid    = "ProtectState"
        Effect = "Deny"
        Action = ["s3:DeleteBucket", "s3:DeleteObject*"]
        Resource = [
          "arn:aws:s3:::gabriel-tf-state-2026",
          "arn:aws:s3:::gabriel-tf-state-2026/*"
        ]
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
