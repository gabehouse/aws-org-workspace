terraform {
  backend "s3" {
    bucket         = "gabriel-tf-state-2026"
    key            = "dev/vstshop/terraform.tfstate"
    region         = "us-east-2"
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
    role_arn       = "arn:aws:iam::086739225244:role/github-actions-oidc-role"
  }
}