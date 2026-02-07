terraform {
  backend "s3" {
    key     = "workloads/dev/networking/terraform.tfstate" # Clean Path!
    encrypt = true
  }
}
