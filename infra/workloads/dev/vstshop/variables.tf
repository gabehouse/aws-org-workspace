variable "project_name" {
  type    = string
  default = "phoenix-vst"
}

variable "environment" {
  type    = string
  default = "dev"
}

variable "region" {
  type    = string
  default = "us-east-2"
}

variable "dev_account_id" { type = string }
# These are just to shut up the auto.tfvars warnings
variable "mgmt_account_id" { type = string }
variable "prod_account_id" { type = string }
