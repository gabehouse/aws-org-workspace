terraform {
  backend "s3" {
    key     = "workloads/prod/wilderchess/terraform.tfstate"
    encrypt = true
  }
}
