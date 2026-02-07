locals {

  external_account_ids = [
    var.dev_account_id,
    var.prod_account_id
  ]
}

# 1. THE BUCKET
resource "aws_s3_bucket" "terraform_state" {
  bucket = var.state_bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

# 2. THE SECURITY (Encryption)
resource "aws_s3_bucket_server_side_encryption_configuration" "state_encryption" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# 3. THE RECOVERY (Versioning)
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# 4. THE LOCK (DynamoDB)
resource "aws_dynamodb_table" "terraform_locks" {
  name         = var.dynamodb_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S" # <--- FIXED: Must be "S", not "S3"
  }
}

# 5. THE PUBLIC BLOCK
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket                  = aws_s3_bucket.terraform_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 6. CROSS-ACCOUNT BUCKET POLICY
# 1. Update the S3 Policy (Already includes Prod via the variable list)
resource "aws_s3_bucket_policy" "state_cross_account" {
  bucket = aws_s3_bucket.terraform_state.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowMultiAccountStateAccess"
        Effect = "Allow",
        Principal = {
          # This maps to ["arn:aws:iam::DEV_ID:root", "arn:aws:iam::PROD_ID:root"]
          AWS = [for id in local.external_account_ids : "arn:aws:iam::${id}:root"]
        },
        Action = [
          "s3:ListBucket",
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ],
        Resource = [
          aws_s3_bucket.terraform_state.arn,
          "${aws_s3_bucket.terraform_state.arn}/*"
        ]
      }
    ]
  })
}

# 2. NEW: DynamoDB Resource-Based Policy
# This is the modern way (post-2024) to allow Dev/Prod to talk to your Management Lock Table
resource "aws_dynamodb_resource_policy" "lock_table_policy" {
  resource_arn = aws_dynamodb_table.terraform_locks.arn
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCrossAccountLocking"
        Effect = "Allow"
        Principal = {
          AWS = [for id in local.external_account_ids : "arn:aws:iam::${id}:root"]
        }
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem"
        ]
        Resource = aws_dynamodb_table.terraform_locks.arn
      }
    ]
  })
}
