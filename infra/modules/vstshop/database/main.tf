resource "aws_dynamodb_table" "purchases" {
  name         = "${var.project_name}-purchases-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"    # Partition Key (The User)
  range_key    = "productId" # Sort Key (The VST)

  attribute {
    name = "userId"
    type = "S"
  }

  attribute {
    name = "productId"
    type = "S"
  }

  tags = {
    Name        = "${var.project_name}-purchases"
    Environment = var.environment
  }
}
