bucket         = "gabriel-tf-state-2026"
region         = "us-east-2"
dynamodb_table = "terraform-state-lock"
assume_role = {
  role_arn = "arn:aws:iam::086739225244:role/github-actions-oidc-role"
}
