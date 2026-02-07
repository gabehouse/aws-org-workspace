variable "region" {
  type    = string
  default = "us-east-2"
}

variable "dev_execution_role_arn" {
  description = "The ARN of the execution role in the Dev account"
  type        = string
}

variable "dev_account_id" { type = string }
variable "mgmt_account_id" { type = string }
variable "prod_account_id" { type = string }
