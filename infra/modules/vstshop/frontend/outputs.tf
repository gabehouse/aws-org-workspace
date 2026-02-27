output "bucket_name" {
  value = aws_s3_bucket.frontend.id
}

output "cloudfront_id" {
  value = aws_cloudfront_distribution.s3_distribution.id
}

output "website_url" {
  value = "https://${aws_cloudfront_distribution.s3_distribution.domain_name}"
}

output "cloudfront_domain_name" {
  value = aws_cloudfront_distribution.s3_distribution.domain_name
}

output "cloudfront_hosted_zone_id" {
  # This is a fixed ID for all CloudFront distributions (Z2FDTNDATAQYW2)
  value = aws_cloudfront_distribution.s3_distribution.hosted_zone_id
}
