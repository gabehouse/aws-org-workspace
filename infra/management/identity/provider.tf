provider "aws" {
  region              = var.region
  allowed_account_ids = [var.mgmt_account_id]
}
