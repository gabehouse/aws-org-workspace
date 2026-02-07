# The Default Provider (Ohio)
provider "aws" {
  region              = var.region
  allowed_account_ids = [var.dev_account_id]
}

# The CloudFront/Certificate Provider (N. Virginia)
# Required because CloudFront certificates MUST live in us-east-1
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}
