# 1. Create the OIDC Provider 
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  # Thumbprint is now optional in modern Terraform AWS providers for GitHub
  thumbprint_list = [] 
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
            # Strictly matches your repo: gabehouse/aws-org-workspace
            "token.actions.githubusercontent.com:sub" = "repo:gabehouse/aws-org-workspace:*"
          }
        }
      }
    ]
  })
}

# 3. Policy for Terraform Backend (S3 + DynamoDB)
# This fixes your "403 Forbidden" error
resource "aws_iam_role_policy" "github_actions_terraform_backend" {
  name = "GitHubActionsBackendAccess"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        # Permission to list the bucket and find the state file
        Effect = "Allow"
        Action = ["s3:ListBucket", "s3:GetBucketLocation"]
        Resource = "arn:aws:s3:::gabriel-tf-state-2026"
      },
      {
        # Permission to read/write the state file itself
        Effect = "Allow"
        Action = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
        Resource = "arn:aws:s3:::gabriel-tf-state-2026/dev/vstshop/terraform.tfstate"
      },
      {
        # Permission to lock the state so others don't overwrite it
        Effect = "Allow"
        Action = [
          "dynamodb:DescribeTable",
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem"
        ]
        Resource = "arn:aws:dynamodb:*:*:table/terraform-state-lock"
      }
    ]
  })
}

# 4. Your existing Role-Chaining Policy
resource "aws_iam_role_policy" "github_actions_assume_role" {
  name = "GitHubActionsAssumeRolePolicy"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action   = "sts:AssumeRole"
        Effect   = "Allow"
        Resource = "arn:aws:iam::*:role/terraform-execution-role-*" 
      }
    ]
  })
}

# 5. Policy for App Deployment (Website S3 + CloudFront)
resource "aws_iam_role_policy" "github_actions_vstshop_deploy" {
  name = "GitHubActionsAppDeploy"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        # Permission to list the bucket (required for 'aws s3 sync')
        Effect   = "Allow"
        Action   = ["s3:ListBucket", "s3:GetBucketLocation"]
        Resource = "arn:aws:s3:::phoenix-vst-frontend-dev"
      },
      {
        # Permission to upload, delete, and update the index.html/assets
        Effect   = "Allow"
        Action   = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject",
          "s3:PutObjectAcl"
        ]
        Resource = "arn:aws:s3:::phoenix-vst-frontend-dev/*"
      },
      {
        # Permission to tell CloudFront to clear its cache
        Effect   = "Allow"
        Action   = ["cloudfront:CreateInvalidation"]
        Resource = "*" # You can narrow this to your specific Distribution ARN later
      }
    ]
  })
}