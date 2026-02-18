import json
import boto3
import os

# Initialize clients outside the handler for performance
dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')

# Get environment variables set by Terraform
TABLE_NAME = os.environ.get('TABLE_NAME')
BUCKET_NAME = os.environ.get('VST_BUCKET_NAME')


def lambda_handler(event, context):
    # 1. Get User ID from Cognito Context
    # Note: For your 'stripe trigger' tests, this will fail unless you're logged in.
    try:
        user_id = event['requestContext']['authorizer']['claims']['sub']
    except (KeyError, TypeError):
        # Fallback for manual testing/debugging without Cognito
        return {
            'statusCode': 401,
            'body': json.dumps({'message': 'Unauthorized: No user context found'})
        }

    # In production, get this from the URL path or query string
    # e.g., /download?productId=cool-synth-v1
    params = event.get('queryStringParameters') or {}
    product_id = params.get('productId', 'cool-synth-v1')

    try:
        table = dynamodb.Table(TABLE_NAME)

        # 2. Check DynamoDB for a purchase record
        # This MUST match the userId and productId written by your Stripe Webhook
        response = table.get_item(
            Key={
                'userId': user_id,
                'productId': product_id
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
                'body': json.dumps({
                    'message': 'Access Denied: Product not purchased.',
                    'debug_userId': user_id  # Remove this in production
                })
            }

        # 3. Generate the S3 Presigned URL
        # The 'Key' must match the file name actually in your S3 bucket
        url = s3.generate_presigned_url(
            'get_object',
            Params={
                'Bucket': BUCKET_NAME,
                # Assuming files are named by product ID
                'Key': f"{product_id}.zip"
            },
            ExpiresIn=3600
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
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'message': 'Internal Server Error', 'error': str(e)})
        }
