terraform {
  backend "s3" {
    bucket         = "gabriel-tf-state-2026"
    key            = "workloads/networking/terraform.tfstate" # A fresh path for a fresh project
    region         = "us-east-2"
    dynamodb_table = "terraform-state-lock"
    profile        = "management" # We use management profile to WRITE to the state bucket
  }
}