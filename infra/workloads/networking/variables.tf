variable "vpc_cidr" {
  type        = string
  description = "The primary IP range for the VPC"
}

variable "public_subnets" {
  type        = list(string)
  description = "Public subnets for Load Balancers"
}

variable "intra_subnets" {
  type        = list(string)
  description = "Isolated subnets for App/DB"
}