terraform {
  backend "s3" {
    key     = "workloads/prod/vstshop/terraform.tfstate"
    encrypt = true
  }
}
