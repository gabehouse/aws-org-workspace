output "s3_bucket_name" {
  value = module.vstshop_frontend.bucket_name
}

output "distribution_id" {
  value = module.vstshop_frontend.cloudfront_id
}

output "website_url" {
  description = "The CloudFront URL to access your shop"
  value       = module.vstshop_frontend.website_url
}

output "auth_config" {
  value = {
    user_pool_id = module.vstshop_auth.user_pool_id
    client_id    = module.vstshop_auth.client_id
    domain       = module.vstshop_auth.auth_domain
  }
}

output "api_url" {
  value = module.vstshop_backend.api_url
}
