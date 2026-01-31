resource "aws_iam_role" "terraform_execution" {
  name = "terraform-execution-role-dev"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        # Trust the Identity Account Role
        AWS = "arn:aws:iam::086739225244:role/github-actions-oidc-role"
      }
    }]
  })
}

# Give this role AdministratorAccess (or specific permissions)
resource "aws_iam_role_policy_attachment" "admin" {
  role       = aws_iam_role.terraform_execution.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}