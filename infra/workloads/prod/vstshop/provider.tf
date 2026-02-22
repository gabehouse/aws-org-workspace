# The Default Provider (Ohio)
provider "aws" {
  region              = module.globals.region
  allowed_account_ids = [module.globals.accounts.prod]
}

# The CloudFront Provider (N. Virginia)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}

# The Stripe Provider
provider "stripe" {
  api_key = var.stripe_secret_key
}
