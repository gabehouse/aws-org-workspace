terraform {
  backend "s3" {
    key     = "workloads/prod/networking/terraform.tfstate"
    encrypt = true
  }
}
