output "s3_bucket_name" {
  value = module.frontend.bucket_name
}

output "distribution_id" {
  value = module.frontend.cloudfront_id
}

output "website_url" {
  description = "The CloudFront URL to access your shop"
  value       = module.frontend.website_url
}

output "auth_config" {
  value = {
    user_pool_id = module.auth.user_pool_id
    client_id    = module.auth.client_id
    domain       = module.auth.auth_domain
  }
}

output "api_url" {
  value = module.backend.api_url
}

output "cloudfront_domain_name" {
  value = module.frontend.cloudfront_domain_name
}

output "cloudfront_hosted_zone_id" {
  value = module.frontend.cloudfront_hosted_zone_id
}
