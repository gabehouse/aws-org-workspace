terraform {
  required_providers {
    stripe = {
      source = "stripe/stripe"
    }
    aws = {
      source = "hashicorp/aws"
    }
  }
}


data "archive_file" "my_purchases_zip" {
  type        = "zip"
  source_file = "${path.module}/lambda/my_purchases.py"
  output_path = "${path.module}/lambda/my_purchases.zip"
}

resource "aws_lambda_function" "my_purchases" {
  function_name    = "${var.project_name}-my-purchases-${var.environment}"
  role             = aws_iam_role.lambda_role.arn
  handler          = "my_purchases.handler"
  runtime          = "python3.12"
  filename         = data.archive_file.my_purchases_zip.output_path
  source_code_hash = data.archive_file.my_purchases_zip.output_base64sha256

  environment {
    variables = {
      TABLE_NAME = var.purchases_table_name
    }
  }
}

resource "aws_api_gateway_resource" "my_purchases" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "my-purchases"
}

resource "aws_api_gateway_method" "my_purchases_get" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.my_purchases.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito.id
}

resource "aws_api_gateway_integration" "my_purchases_int" {
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.my_purchases.id
  http_method             = aws_api_gateway_method.my_purchases_get.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.my_purchases.invoke_arn
}

resource "aws_lambda_permission" "api_gw_my_purchases" {
  statement_id  = "AllowExecutionFromAPIGatewayPurchases"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.my_purchases.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.api.execution_arn}/*/*"
}

module "my_purchases_cors" {
  source          = "squidfunk/api-gateway-enable-cors/aws"
  version         = "0.3.3"
  api_id          = aws_api_gateway_rest_api.api.id
  api_resource_id = aws_api_gateway_resource.my_purchases.id
  allow_origin    = "*"
  allow_headers   = ["Authorization", "Content-Type"]
}

### --- DOWNLOAD LAMBDA (Secure Proxy) --- ###

# A. Zip the download code
data "archive_file" "download_zip" {
  type        = "zip"
  source_file = "${path.module}/lambda/download.py"
  output_path = "${path.module}/lambda/download.zip"
}

# B. The Lambda Function
resource "aws_lambda_function" "download" {
  function_name    = "${var.project_name}-download-${var.environment}"
  role             = aws_iam_role.lambda_role.arn
  handler          = "download.handler"
  runtime          = "python3.12"
  filename         = data.archive_file.download_zip.output_path
  source_code_hash = data.archive_file.download_zip.output_base64sha256

  environment {
    variables = {
      TABLE_NAME     = var.purchases_table_name
      STORAGE_BUCKET = var.vst_bucket_name
      S3_KEY_MAP = jsonencode({
        for k, v in local.vst_catalog : k => v.s3_key
      })
    }
  }
}

resource "aws_lambda_permission" "apigw_download" {
  statement_id  = "AllowExecutionFromAPIGatewayDownload"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.download.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.api.execution_arn}/*/*"
}

resource "aws_api_gateway_gateway_response" "default_4xx" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  response_type = "DEFAULT_4XX"

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Origin"  = "'*'"
    "gatewayresponse.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "gatewayresponse.header.Access-Control-Allow-Methods" = "'GET,POST,OPTIONS'"
  }
}

### --- API GATEWAY ROUTE (/download) --- ###

resource "aws_api_gateway_resource" "download" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "download"
}

resource "aws_api_gateway_method" "download_get" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.download.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito.id
}

resource "aws_api_gateway_integration" "download_integration" {
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.download.id
  http_method             = aws_api_gateway_method.download_get.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.download.invoke_arn
}

# Enable CORS for /download
module "download_cors" {
  source  = "squidfunk/api-gateway-enable-cors/aws"
  version = "0.3.3"

  api_id          = aws_api_gateway_rest_api.api.id
  api_resource_id = aws_api_gateway_resource.download.id
  allow_origin    = "*"
  allow_headers   = ["Authorization", "Content-Type"]
}

### --- CREATE CHECKOUT LAMBDA --- ###

# A. Zip the checkout code
data "archive_file" "checkout_zip" {
  type        = "zip"
  source_file = "${path.module}/lambda/checkout.py" # UPDATED
  output_path = "${path.module}/lambda/checkout.zip"
}

# B. The Lambda Function
resource "aws_lambda_function" "checkout" {
  function_name    = "${var.project_name}-checkout-${var.environment}"
  role             = aws_iam_role.lambda_role.arn
  handler          = "checkout.handler" # UPDATED (filename.function)
  runtime          = "python3.12"
  filename         = data.archive_file.checkout_zip.output_path
  source_code_hash = data.archive_file.checkout_zip.output_base64sha256

  layers = [aws_lambda_layer_version.stripe_layer.arn]

  environment {
    variables = {
      STRIPE_SECRET_KEY     = var.stripe_secret_key
      STRIPE_WEBHOOK_SECRET = var.stripe_webhook_secret
      FRONTEND_URL          = var.cloudfront_url
      STRIPE_PRODUCT_MAP = jsonencode({
        for k, v in stripe_price.vst_price : k => v.id
      })
    }
  }
}

resource "aws_lambda_permission" "apigw_checkout" {
  statement_id  = "AllowExecutionFromAPIGatewayCheckout"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.checkout.function_name
  principal     = "apigateway.amazonaws.com"

  # This allows any stage/method in this API to call this Lambda
  source_arn = "${aws_api_gateway_rest_api.api.execution_arn}/*/*"
}

### --- API GATEWAY ROUTE (/checkout) --- ###

resource "aws_api_gateway_resource" "checkout" {
  rest_api_id = aws_api_gateway_rest_api.api.id # Changed from .main to .api to match your file
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "checkout"
}

resource "aws_api_gateway_method" "checkout_post" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.checkout.id
  http_method   = "POST"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito.id
}

resource "aws_api_gateway_integration" "checkout_integration" {
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.checkout.id
  http_method             = aws_api_gateway_method.checkout_post.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.checkout.invoke_arn
}

# C. Enable CORS for /checkout
module "checkout_cors" {
  source  = "squidfunk/api-gateway-enable-cors/aws"
  version = "0.3.3"

  api_id          = aws_api_gateway_rest_api.api.id
  api_resource_id = aws_api_gateway_resource.checkout.id
  allow_origin    = "*" # Or your specific Vercel/S3 URL
  allow_headers   = ["Authorization", "Content-Type"]
}

# NEW: This ensures the 'stripe' library actually exists before zipping
resource "null_resource" "install_dependencies" {
  provisioner "local-exec" {
    command = "mkdir -p ${path.module}/layer/python && pip install stripe -t ${path.module}/layer/python"
  }

  # This makes it only run once, unless you manually delete the folder
  triggers = {
    always_run = "${path.module}/layer/python/stripe"
  }
}


# --- 1. Zip the layer folder ---
data "archive_file" "stripe_layer_zip" {
  type        = "zip"
  source_dir  = "${path.module}/layer"
  output_path = "${path.module}/stripe_layer.zip"

  # CRITICAL: Ensures pip install finishes before we try to zip it!
  depends_on = [null_resource.install_dependencies]
}

# --- 2. Create the Layer ---
resource "aws_lambda_layer_version" "stripe_layer" {
  filename            = data.archive_file.stripe_layer_zip.output_path
  layer_name          = "${var.project_name}-stripe-layer-${var.environment}"
  source_code_hash    = data.archive_file.stripe_layer_zip.output_base64sha256
  compatible_runtimes = ["python3.12"]
}

### --- STRIPE WEBHOOK LAMBDA --- ###

data "archive_file" "webhook_zip" {
  type        = "zip"
  source_file = "${path.module}/lambda/stripe_webhook.py"
  output_path = "${path.module}/lambda/stripe_webhook.zip"
}

resource "aws_lambda_function" "stripe_webhook" {
  function_name    = "${var.project_name}-stripe-webhook-${var.environment}"
  role             = aws_iam_role.lambda_role.arn
  handler          = "stripe_webhook.lambda_handler"
  runtime          = "python3.12"
  filename         = data.archive_file.webhook_zip.output_path
  source_code_hash = data.archive_file.webhook_zip.output_base64sha256
  layers           = [aws_lambda_layer_version.stripe_layer.arn]

  environment {
    variables = {
      TABLE_NAME            = var.purchases_table_name
      STRIPE_SECRET_KEY     = var.stripe_secret_key
      STRIPE_WEBHOOK_SECRET = var.stripe_webhook_secret
    }
  }
}

### --- API GATEWAY ROUTE (/webhook) --- ###

resource "aws_api_gateway_resource" "webhook" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "webhook"
}

resource "aws_api_gateway_method" "webhook_post" {
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.webhook.id
  http_method   = "POST"
  authorization = "NONE" # CRITICAL: Stripe needs public access
}

resource "aws_api_gateway_integration" "webhook_integration" {
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.webhook.id
  http_method             = aws_api_gateway_method.webhook_post.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.stripe_webhook.invoke_arn
}

resource "aws_lambda_permission" "apigw_webhook" {
  statement_id  = "AllowExecutionFromAPIGatewayWebhook"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.stripe_webhook.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.api.execution_arn}/*/*"
}


### --- 1. API GATEWAY CORE --- ###

resource "aws_api_gateway_rest_api" "api" {
  name        = "${var.project_name}-api-${var.environment}"
  description = "Main API for the VST Shop"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_authorizer" "cognito" {
  name            = "vstshop-cognito-authorizer"
  rest_api_id     = aws_api_gateway_rest_api.api.id
  type            = "COGNITO_USER_POOLS"
  provider_arns   = [var.user_pool_arn]
  identity_source = "method.request.header.Authorization"
}

### --- 4. DEPLOYMENT & STAGE --- ###

resource "aws_api_gateway_deployment" "deployment" {
  rest_api_id = aws_api_gateway_rest_api.api.id

  triggers = {
    # Redeploy only when the structure of the API changes
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.my_purchases.id,
      aws_api_gateway_method.my_purchases_get.id,
      aws_api_gateway_integration.my_purchases_int.id,
      aws_api_gateway_resource.webhook.id,
      aws_api_gateway_method.webhook_post.id,
      aws_api_gateway_integration.webhook_integration.id,
      aws_api_gateway_resource.checkout.id,
      aws_api_gateway_method.checkout_post.id,
      aws_api_gateway_integration.checkout_integration.id,
      aws_api_gateway_resource.download.id,
      aws_api_gateway_method.download_get.id,
      aws_api_gateway_integration.download_integration.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }

  # CRITICAL: Ensures all integrations are ready before deploying
  depends_on = [
    aws_api_gateway_integration.webhook_integration,
    aws_api_gateway_integration.checkout_integration,
    aws_api_gateway_integration.download_integration
  ]
}

resource "aws_api_gateway_stage" "this" {
  deployment_id = aws_api_gateway_deployment.deployment.id
  rest_api_id   = aws_api_gateway_rest_api.api.id
  stage_name    = var.environment
}

### --- 5. IAM ROLES & POLICIES (Consolidated) --- ###

resource "aws_iam_role" "lambda_role" {
  name = "${var.project_name}-lambda-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_iam_policy" "lambda_combined_policy" {
  name        = "${var.project_name}-lambda-combined-policy-${var.environment}"
  description = "Permissions for CloudWatch, DynamoDB (Purchases), and S3 Presigning"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      # 1. CloudWatch Logs
      {
        Effect   = "Allow"
        Action   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"]
        Resource = "arn:aws:logs:*:*:*"
      },
      # 2. DynamoDB Access
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:Query"
        ]
        Resource = [
          var.purchases_table_arn,
          "${var.purchases_table_arn}/index/*"
        ]
      },
      # 3. S3 GetObject (for Presigning)
      {
        Action   = ["s3:GetObject"]
        Effect   = "Allow"
        Resource = "arn:aws:s3:::${var.vst_bucket_name}/*"
      },
      {
        Effect   = "Allow"
        Action   = ["s3:ListBucket"]
        Resource = "arn:aws:s3:::${var.vst_bucket_name}" # The Bucket itself (No /*)
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_attach" {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_combined_policy.arn
}
