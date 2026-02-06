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
          AWS = "arn:aws:iam::086739225244:role/github-actions-oidc-role"
        }
      },
      {
        Sid = "AllowLocalSSO"
        # Adding TagSession here too for "Dev Container" parity
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::195481994910:root"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "admin" {
  role       = aws_iam_role.terraform_execution.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
