variable "environment" { type = string }
variable "project_name" {
  type    = string
  default = "vstshop"
}

variable "aliases" {
  type    = list(string)
  default = []
}

variable "acm_certificate_arn" {
  type    = string
  default = null
}
