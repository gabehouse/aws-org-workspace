provider "aws" {
  region              = "us-east-2"
  allowed_account_ids = [var.dev_account_id]
}
