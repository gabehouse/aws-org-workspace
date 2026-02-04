terraform {
  backend "s3" {
    bucket         = "gabriel-tf-state-2026"
    key            = "workloads/dev/iam/terraform.tfstate"
    region         = "us-east-2"
    dynamodb_table = "arn:aws:dynamodb:us-east-2:086739225244:table/terraform-state-lock"
    encrypt        = true
  }
}