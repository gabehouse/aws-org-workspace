terraform {
  backend "s3" {
    key     = "workloads/prod/iam/terraform.tfstate"
    encrypt = true
  }
}
