terraform {
  backend "s3" {
    key     = "workloads/dev/networking/terraform.tfstate"
    encrypt = true
  }
}
