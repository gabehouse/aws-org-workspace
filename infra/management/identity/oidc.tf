# 1. THE FOUNDATION: Trust GitHub's Certificates
resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
  # Note: In 2026, AWS often ignores this thumbprint, but Terraform
  # still requires a valid one to be passed in.
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

# 2. THE GATEWAY: The role GitHub "Logs Into" (Hub Account)
resource "aws_iam_role" "github_actions" {
  name = "github-actions-oidc-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowGitHubOIDC"
        Effect = "Allow"
        Principal = {
          # Use the reference to ensure account number and path are perfect
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Action = ["sts:AssumeRoleWithWebIdentity", "sts:TagSession"]
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" : "repo:gabehouse/aws-org-workspace:*"
          }
        }
      }
    ]
  })
}

# 3. THE PERMISSION: The "Keycard" to Jump to Dev (Hub Account)
resource "aws_iam_role_policy" "github_actions_permissions" {
  name = "github-actions-permissions"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowAssumeWorkloadRoles"
        Effect = "Allow"
        Action = [
          "sts:AssumeRole",
          "sts:TagSession"
        ]
        # Pointing to your Spoke account
        Resource = [
          "arn:aws:iam::${module.globals.accounts.dev}:role/terraform-execution-role-dev",
          "arn:aws:iam::${module.globals.accounts.prod}:role/terraform-execution-role-prod"
        ]
      }
    ]
  })
}
