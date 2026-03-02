terraform {
  backend "s3" {
    key     = "workloads/dev/wilderchess/terraform.tfstate"
    encrypt = true
  }
}
