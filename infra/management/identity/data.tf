# 1. Read the Org Account IDs from the other state file
data "terraform_remote_state" "org" {
  backend = "s3"
  config = {
    bucket = "gabriel-tf-state-2026"
    key    = "management/organization/terraform.tfstate"
    region = "us-east-2"
  }
}

# 2. Find the SSO Instance (AWS only allows one)
data "aws_ssoadmin_instances" "main" {}

# 3. Find your User (the one you made manually)
data "aws_identitystore_user" "me" {
  identity_store_id = data.aws_ssoadmin_instances.main.identity_store_ids[0]
  alternate_identifier {
    unique_attribute {
      attribute_path  = "UserName"
      attribute_value = "Gabe" # Change to your actual SSO username
    }
  }
}
