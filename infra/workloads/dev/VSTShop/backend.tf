terraform {
  backend "s3" {
    bucket         = "gabriel-tf-state-2026"
    key            = "dev/vstshop/terraform.tfstate"
    region         = "us-east-2"
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
  }
}