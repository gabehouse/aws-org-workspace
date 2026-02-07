terraform {
  backend "s3" {
    key     = "management/organization/terraform.tfstate"
    encrypt = true
  }
}
