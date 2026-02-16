module "globals" {
  source = "../../../modules/globals"
}

module "frontend" {
  source       = "../../../modules/vstshop/frontend"
  project_name = "vstshop"
  environment  = var.environment
}

moved {
  from = module.vstshop_frontend
  to   = module.frontend
}

module "auth" {
  source       = "../../../modules/vstshop/auth"
  environment  = var.environment
  project_name = "vstshop"
  unique_id    = "vst-prod-123" # To ensure unique Cognito domain

  # Dynamically pass the CloudFront URL for Auth redirects
  callback_url = module.frontend.website_url
}

moved {
  from = module.vstshop_auth
  to   = module.auth
}

module "backend" {
  source          = "../../../modules/vstshop/backend"
  environment     = var.environment
  project_name    = "vstshop"
  user_pool_arn   = module.auth.user_pool_arn # Passing the ARN here
  vst_bucket_name = module.storage.vst_bucket_name
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
  EOT
}
