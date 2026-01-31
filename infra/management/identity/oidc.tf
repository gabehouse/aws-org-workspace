# 1. Create the OIDC Provider (The "Trust" link to GitHub)
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  # This thumbprint is standard for GitHub's certificate
  thumbprint_list = ["1b51137351137351137351137351137351137351"] 
}

# 2. Create the GitHub Action Role
resource "aws_iam_role" "github_actions" {
  name = "github-actions-oidc-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRoleWithWebIdentity"
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:gabehouse/aws-org-workspace:*"
          }
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "github_actions_assume_role" {
  name = "GitHubActionsAssumeRolePolicy"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action   = "sts:AssumeRole"
        Effect   = "Allow"
        # This allows the Identity role to jump into ANY account 
        # as long as the target role allows it.
        Resource = "arn:aws:iam::*:role/terraform-execution-role-*" 
      }
    ]
  })
}