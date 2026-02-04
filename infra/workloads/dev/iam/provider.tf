# The Default Provider (Ohio)
provider "aws" {
  region = "us-east-2"
}

# The CloudFront/Certificate Provider (N. Virginia)
# Required because CloudFront certificates MUST live in us-east-1
provider "aws" {
  alias  = "us_east_1" 
  region = "us-east-1"
}