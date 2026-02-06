resource "aws_iam_role" "terraform_execution" {
  name = "terraform-execution-role-dev"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowGitHubOIDC"
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          # Trust the Identity Account Role (GitHub Actions)
          AWS = "arn:aws:iam::086739225244:role/github-actions-oidc-role"
        }
      },
      {
        Sid    = "AllowLocalSSO"
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          # Trusting the local account root allows your SSO Admin
          # to assume this role when working from your terminal.
          AWS = "arn:aws:iam::195481994910:root"
        }
      }
    ]
  })
}

# Give this role AdministratorAccess
resource "aws_iam_role_policy_attachment" "admin" {
  role       = aws_iam_role.terraform_execution.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
