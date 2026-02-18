variable "environment" {
  type    = string
  default = "dev"
}
variable "project_name" {
  type    = string
  default = "vstshop"
}

variable "stripe_secret_key" {
  type      = string
  sensitive = true
}

variable "stripe_webhook_secret" {
  type      = string
  sensitive = true
}
