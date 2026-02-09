module "globals" {
  source = "../../../modules/globals"
}

module "vstshop" {
  source = "../../../modules/vstshop-frontend"

  project_name        = "vstshop"
  environment         = "dev"
  mgmt_account_id     = module.globals.accounts.mgmt
  workload_account_id = module.globals.accounts.dev
}

# These pass the module outputs to the GitHub Action
output "s3_bucket_name" {
  value = module.vstshop.bucket_name
}

output "distribution_id" {
  value = module.vstshop.cloudfront_id
}
