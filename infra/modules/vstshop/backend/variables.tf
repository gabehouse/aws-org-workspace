variable "environment" { type = string }
variable "project_name" {
  type    = string
  default = "vstshop"
}
variable "user_pool_arn" { type = string }
variable "vst_bucket_name" { type = string }
variable "purchases_table_name" { type = string }
variable "purchases_table_arn" { type = string }
variable "stripe_secret_key" {
  type      = string
  sensitive = true
}
variable "stripe_webhook_secret" {
  type      = string
  sensitive = true
}
variable "cloudfront_url" { type = string }
