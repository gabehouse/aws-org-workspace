# The Default Provider (Ohio)
provider "aws" {
  region              = module.globals.region
  allowed_account_ids = [module.globals.accounts.dev]
}
