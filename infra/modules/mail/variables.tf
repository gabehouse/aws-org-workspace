variable "is_enabled" {
  type        = bool
  description = "Toggle to enable/disable mail infrastructure (use false for dev without domain)"
}

variable "domain_name" {
  type = string
}

variable "route53_zone_id" {
  type = string
}

variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "personal_email" {
  type        = string
  description = "Where to forward the support emails"
}
