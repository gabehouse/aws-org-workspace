terraform {
  backend "s3" {
    key     = "workloads/dev/vstshop/terraform.tfstate"
    encrypt = true
  }
}
