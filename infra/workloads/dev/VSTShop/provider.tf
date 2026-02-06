# The Default Provider (Ohio)
provider "aws" {
  region = var.region
}

# The CloudFront/Certificate Provider (N. Virginia)
provider "aws" {
  alias  = "us_east_1" # This is the "nickname"
  region = "us-east-1"
}
