variable "state_bucket_name" {
  type    = string
  default = "gabriel-tf-state-2026"

  validation {
    condition     = can(regex("^[a-z0-9.-]{3,63}$", var.state_bucket_name))
    error_message = "S3 bucket names must be lowercase and between 3-63 characters."
  }
}

variable "dynamodb_table_name" {
  type    = string
  default = "terraform-state-lock"
}
