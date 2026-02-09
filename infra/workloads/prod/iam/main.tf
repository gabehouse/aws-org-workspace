module "globals" {
  source = "../../../modules/globals"
}

resource "aws_iam_role" "terraform_execution" {
  name = module.globals.prod_execution_role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid = "AllowGitHubOIDC"
        # Explicitly allowing TagSession ensures role-chaining works smoothly
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${module.globals.accounts.mgmt}:role/${module.globals.github_gateway_role_name}"

        }
      },
      {
        Sid = "AllowLocalSSO"
        # Adding TagSession here too for "Dev Container" parity
        Action = ["sts:AssumeRole", "sts:TagSession"]
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${module.globals.accounts.prod}:root"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "admin" {
  role       = aws_iam_role.terraform_execution.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
