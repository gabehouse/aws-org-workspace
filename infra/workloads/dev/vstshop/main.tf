module "globals" {
  source = "../../../modules/globals"
}

module "frontend" {
  source       = "../../../modules/vstshop/frontend"
  project_name = var.project_name
  environment  = var.environment
}

moved {
  from = module.vstshop_frontend
  to   = module.frontend
}

module "auth" {
  source       = "../../../modules/vstshop/auth"
  environment  = var.environment
  project_name = var.project_name
  unique_id    = "vst-dev-123" # To ensure unique Cognito domain

  # Dynamically pass the CloudFront URL for Auth redirects
  callback_url = module.frontend.website_url
}

moved {
  from = module.vstshop_auth
  to   = module.auth
}

# 1. Create the Database
module "database" {
  source       = "../../../modules/vstshop/database"
  project_name = var.project_name
  environment  = var.environment
}

# 2. Pass database outputs into the Backend
module "backend" {
  source          = "../../../modules/vstshop/backend"
  project_name    = var.project_name
  environment     = var.environment
  vst_bucket_name = module.storage.vst_bucket_name
  user_pool_arn   = module.auth.user_pool_arn

  # These link the modules together:
  purchases_table_name = module.database.table_name
  purchases_table_arn  = module.database.table_arn
}

moved {
  from = module.vstshop_backend
  to   = module.backend
}

module "storage" {
  source      = "../../../modules/vstshop/storage"
  domain_name = module.frontend.website_url
  environment = var.environment
}

# This tells Terraform to write a file on your local machine
resource "local_file" "env_file" {
  filename = "${path.module}/../../../../services/vstshop-frontend/.env"
  content  = <<-EOT
    VITE_AWS_REGION=${module.globals.region}
    VITE_USER_POOL_ID=${module.auth.user_pool_id}
    VITE_USER_POOL_CLIENT_ID=${module.auth.client_id}
    VITE_API_URL=${module.backend.api_url}
  EOT
}
