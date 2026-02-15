output "user_pool_id" {
  value = aws_cognito_user_pool.pool.id
}

output "client_id" {
  value = aws_cognito_user_pool_client.client.id
}

output "auth_domain" {
  value = aws_cognito_user_pool_domain.main.domain
}

output "user_pool_arn" {
  value       = aws_cognito_user_pool.pool.arn
  description = "The ARN of the Cognito User Pool for the API Gateway Authorizer"
}
