terraform {
  required_version = ">= 1.5.0" # Fixes 'terraform_required_version'

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0" # Fixes 'terraform_required_providers'
    }
  }
}
