import json
import boto3
import os

# Initialize clients outside the handler for best performance
dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')

# Standardized Key Names (matches your Webhook Lambda)
# Table name can be hardcoded or from Env Var
TABLE_NAME = os.environ.get('TABLE_NAME', 'vstshop-purchases-dev')
BUCKET_NAME = os.environ.get('VST_BUCKET_NAME')


def lambda_handler(event, context):
    # 1. Get User ID from Cognito Context
    # NOTE: In production, the Cognito 'sub' is the Partition Key
    try:
        user_id = event['requestContext']['authorizer']['claims']['sub']
    except (KeyError, TypeError):
        # Useful for testing in the console without a real Cognito Token
        return {
            'statusCode': 401,
            'body': json.dumps({'message': 'Unauthorized: No user session found'})
        }

    # In a real app, this would come from: event['queryStringParameters']['productId']
    # Hardcoded for now as you requested
    product_id = "cool-synth-v1"

    try:
        table = dynamodb.Table(TABLE_NAME)

        # 2. Check DynamoDB for a purchase record
        # 🚨 CRITICAL: Check your Webhook Lambda. Did you use 'userId' or 'user_id'?
        # DynamoDB is CASE-SENSITIVE.
        response = table.get_item(
            Key={
                'userId': user_id,      # Changed to 'userId' to match typical camelCase setup
                'productId': product_id  # Changed to 'productId'
            }
        )

        if 'Item' not in response:
            return {
                'statusCode': 403,
                'headers': {
                    "Access-Control-Allow-Origin": "*",
                    "Access-Control-Allow-Headers": "Content-Type,Authorization",
                    "Access-Control-Allow-Methods": "GET,OPTIONS"
                },
                'body': json.dumps({'message': 'Access Denied: Product not purchased.'})
            }

        # 3. If they paid, generate the S3 Presigned URL
        url = s3.generate_presigned_url(
            'get_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': f"{product_id}.zip"  # Dynamic key based on product
            },
            ExpiresIn=3600  # 1 Hour
        )

        return {
            'statusCode': 200,
            'headers': {
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Headers": "Content-Type,Authorization",
                "Access-Control-Allow-Methods": "GET,OPTIONS"
            },
            'body': json.dumps({'downloadUrl': url})
        }

    except Exception as e:
        print(f"ERROR: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'message': 'Internal Server Error', 'error': str(e)})
        }
