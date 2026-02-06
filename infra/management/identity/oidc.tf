resource "aws_iam_role" "github_actions" {
  name = "github-actions-oidc-role"

  # THIS IS ONLY THE HANDSHAKE (No "Resource" field allowed here!)
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowGitHubOIDC"
        Effect = "Allow"
        Principal = {
          Federated = "arn:aws:iam::086739225244:oidc-provider/token.actions.githubusercontent.com"
        }
        Action = ["sts:AssumeRoleWithWebIdentity", "sts:TagSession"]
        Condition = {
          StringEquals = { "token.actions.githubusercontent.com:aud" : "sts.amazonaws.com" }
          StringLike   = { "token.actions.githubusercontent.com:sub" : "repo:gabehouse/aws-org-workspace:*" }
        }
      }
    ]
  })
}

# THIS IS THE PERMISSION (This is where "Resource" belongs!)
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
        # This is where you point to your Dev account role
        Resource = "arn:aws:iam::195481994910:role/terraform-execution-role-dev"
      }
    ]
  })
}
