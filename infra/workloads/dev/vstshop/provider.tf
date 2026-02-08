# The Default Provider (Ohio)
provider "aws" {
  region              = module.globals.region
  allowed_account_ids = [module.globals.accounts.dev]
}

# The CloudFront/Certificate Provider (N. Virginia)
provider "aws" {
  alias  = "us_east_1" # This is the "nickname"
  region = "us-east-1"
}
