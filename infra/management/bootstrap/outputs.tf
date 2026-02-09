output "state_bucket_arn" {
  description = "The ARN of the S3 bucket for state"
  value       = aws_s3_bucket.terraform_state.arn
}

output "state_bucket_name" {
  description = "The name of the S3 bucket for state"
  value       = aws_s3_bucket.terraform_state.id
}
