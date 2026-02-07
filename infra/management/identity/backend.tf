terraform {
  backend "s3" {
    key     = "management/identity/terraform.tfstate"
    encrypt = true
  }
}
