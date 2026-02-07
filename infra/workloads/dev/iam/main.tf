resource "aws_iam_role" "terraform_execution" {
  name = "terraform-execution-role-dev"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid = "AllowGitHubOIDC"
        # Explicitly allowing TagSession ensures role-chaining works smoothly
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          AWS = var.github_actions_oidc_execution_role_arn
        }
      },
      {
        Sid = "AllowLocalSSO"
        # Adding TagSession here too for "Dev Container" parity
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          AWS = var.dev_account_root_arn
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "admin" {
  role       = aws_iam_role.terraform_execution.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
