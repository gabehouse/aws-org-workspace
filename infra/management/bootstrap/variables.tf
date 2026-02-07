variable "aws_region" {
  type    = string
  default = "us-east-2"
}

variable "state_bucket_name" {
  type    = string
  default = "gabriel-tf-state-2026" # Ensure this is unique!

  validation {
    condition     = can(regex("^[a-z0-9.-]{3,63}$", var.state_bucket_name))
    error_message = "S3 bucket names must be lowercase and between 3-63 characters."
  }
}

variable "dynamodb_table_name" {
  type    = string
  default = "terraform-state-lock"
}


variable "dev_account_id" { type = string }
# These are just to shut up the auto.tfvars warnings
variable "mgmt_account_id" { type = string }
variable "prod_account_id" { type = string }

variable "region" { type = string }
