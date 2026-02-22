terraform {
  required_version = ">= 1.5" # Fixes 'terraform_required_version'

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0" # Fixes 'terraform_required_providers'
    }
    stripe = {
      source  = "stripe/stripe"
      version = "0.1.3"
    }
  }
}
