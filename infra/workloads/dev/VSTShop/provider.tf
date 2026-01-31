# The Default Provider (Ohio)
provider "aws" {
  region = var.region
  assume_role {
    # This tells Terraform: "Once GitHub logs me in, 
    # immediately switch to this powerful execution role."
    role_arn = "arn:aws:iam::DEV_ACCOUNT_ID:role/terraform-execution-role-dev"
  }
}

# The CloudFront/Certificate Provider (N. Virginia)
provider "aws" {
  alias   = "us_east_1" # This is the "nickname"
  region  = "us-east-1"
  profile = "dev"
}