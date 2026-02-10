# 1. Find the SSO Instance (AWS only allows one)
data "aws_ssoadmin_instances" "main" {}

# 2. Find your User (the one you made manually)
data "aws_identitystore_user" "me" {
  identity_store_id = data.aws_ssoadmin_instances.main.identity_store_ids[0]
  alternate_identifier {
    unique_attribute {
      attribute_path  = "UserName"
      attribute_value = module.globals.sso_admin_user
    }
  }
}
