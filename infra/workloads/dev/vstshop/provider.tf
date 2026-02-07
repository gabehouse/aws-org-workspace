# The Default Provider (Ohio)
provider "aws" {
  region              = var.region
  allowed_account_ids = [var.dev_account_id]
}

# The CloudFront/Certificate Provider (N. Virginia)
provider "aws" {
  alias  = "us_east_1" # This is the "nickname"
  region = "us-east-1"
}
