module "globals" {
  source = "../../../modules/globals"
}

module "vstshop" {
  source = "../../../modules/vstshop-frontend"

  project_name        = "vstshop"
  environment         = "prod"
  mgmt_account_id     = module.globals.accounts.mgmt
  workload_account_id = module.globals.accounts.prod
}

# These pass the module outputs to the GitHub Action
output "s3_bucket_name" {
  value = module.vstshop.bucket_name
}

output "distribution_id" {
  value = module.vstshop.cloudfront_id
}

output "website_url" {
  description = "The CloudFront URL to access your shop"
  value       = "https://${module.vstshop.cloudfront_domain_name}"
}
