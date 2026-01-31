output "website_url" {
  description = "The CloudFront URL to access your shop"
  value       = "https://${aws_cloudfront_distribution.s3_distribution.domain_name}"
}

output "s3_bucket_name" {
  description = "The name of the bucket (for deployment scripts)"
  value       = aws_s3_bucket.frontend.id
}

output "distribution_id" {
  description = "CloudFront Distribution ID (for clearing cache)"
  value       = aws_cloudfront_distribution.s3_distribution.id
}