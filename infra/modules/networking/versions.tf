terraform {
  # Ensures everyone on your team uses at least this version of Terraform
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source = "hashicorp/aws"
      # ~> 6.0 means: "Allow 6.28, 6.29, etc., but do not jump to 7.0 automatically"
      version = "~> 6.0"
    }
  }
}
