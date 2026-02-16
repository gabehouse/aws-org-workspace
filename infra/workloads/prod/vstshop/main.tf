module "globals" {
  source = "../../../modules/globals"
}

module "vstshop_frontend" {
  source       = "../../../modules/vstshop-frontend"
  project_name = "vstshop"
  environment  = var.env
}

moved {
  from = module.vstshop
  to   = module.vstshop_frontend
}

module "vstshop_auth" {
  source       = "../../../modules/vstshop-auth"
  environment  = var.env
  project_name = "vstshop"
  unique_id    = "vst-prod-123" # To ensure unique Cognito domain

  # Dynamically pass the CloudFront URL for Auth redirects
  callback_url = module.vstshop_frontend.website_url
}

module "vstshop_backend" {
  source        = "../../../modules/vstshop-backend"
  environment   = var.env
  project_name  = "vstshop"
  user_pool_arn = module.vstshop_auth.user_pool_arn # Passing the ARN here
}

# This tells Terraform to write a file on your local machine
resource "local_file" "env_file" {
  filename = "${path.module}/../../../../services/vstshop-frontend/.env"
  content  = <<-EOT
    VITE_AWS_REGION=${module.globals.region}
    VITE_USER_POOL_ID=${module.vstshop_auth.user_pool_id}
    VITE_USER_POOL_CLIENT_ID=${module.vstshop_auth.client_id}
  EOT
}
