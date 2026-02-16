output "bucket_name" {
  value = aws_s3_bucket.frontend.id
}

output "cloudfront_id" {
  value = aws_cloudfront_distribution.s3_distribution.id
}

output "website_url" {
  value = "https://${aws_cloudfront_distribution.s3_distribution.domain_name}"
}
