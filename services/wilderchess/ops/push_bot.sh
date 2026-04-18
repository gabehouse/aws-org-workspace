#!/bin/bash

# ---- CONFIGURATION ----
AWS_REGION="us-east-2"
AWS_ACCOUNT_ID="195481994910"
REPO_NAME="wilderchess-bot-runner-dev"
IMAGE_TAG="latest"

# Full ECR URI
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO_NAME}:${IMAGE_TAG}"

echo "🚀 Starting Bot Factory Deployment..."

# 1. Authenticate Docker to ECR
echo "🔐 Authenticating with ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

if [ $? -ne 0 ]; then
    echo "❌ ECR Login Failed. Check your AWS credentials."
    exit 1
fi

# 2. Build the Docker Image
echo "🛠️ Building Docker image: ${REPO_NAME}..."
docker build -t $REPO_NAME .

if [ $? -ne 0 ]; then
    echo "❌ Docker Build Failed. Check your Dockerfile."
    exit 1
fi

# 3. Tag the image for ECR
echo "🏷️ Tagging image as ${IMAGE_TAG}..."
docker tag "${REPO_NAME}:latest" "$ECR_URI"

# 4. Push to ECR
echo "📤 Pushing to ECR: ${ECR_URI}..."
docker push "$ECR_URI"

if [ $? -eq 0 ]; then
    echo "✅ Success! Your bot is now in ECR."
    echo "💡 Note: Your EC2 instances will pull this automatically on their next reboot/scale-up."
else
    echo "❌ Push failed."
    exit 1
fi
