variable "region" {
  description = "AWS Region"
  type        = string
  default     = "us-east-2"
}

variable "vpc_name" {
  description = "Name of the VPC"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
}

variable "public_subnets" {
  description = "List of public subnet CIDRs"
  type        = list(string)
}

variable "intra_subnets" {
  description = "List of intra (private no-NAT) subnet CIDRs"
  type        = list(string)
}

variable "tags" {
  description = "Common tags for all resources"
  type        = map(string)
  default     = {}
}
