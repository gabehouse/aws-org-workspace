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
