module "globals" {
  source = "../../../modules/globals"
}

module "vstshop" {
  source      = "../../../modules/vstshop-frontend"
  environment = "dev"
}

output "s3_bucket_name" {
  value = module.vstshop.bucket_name
}

output "distribution_id" {
  value = module.vstshop.cloudfront_id
}

output "website_url" {
  description = "The CloudFront URL to access your shop"
  value       = module.vstshop.website_url
}
