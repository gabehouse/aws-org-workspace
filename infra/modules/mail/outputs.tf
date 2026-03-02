output "bucket_name" {
  description = "The name of the bucket where raw emails are stored"
  value       = var.is_enabled ? aws_s3_bucket.email_storage[0].bucket : null
}
