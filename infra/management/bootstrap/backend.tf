terraform {
  backend "s3" {
    key     = "management/bootstrap/terraform.tfstate"
    encrypt = true
  }
}
