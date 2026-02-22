import os
import json
import boto3
from botocore.exceptions import ClientError

# Initialize AWS Clients
dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')

# Environment Variables
TABLE_NAME = os.environ['TABLE_NAME']
BUCKET_NAME = os.environ['STORAGE_BUCKET']

# Load the S3 Key Map from Terraform
# Example: {"cool-synth-v1": "vsts/synth.zip", "retro-verb-v1": "vsts/verb.zip"}
S3_KEY_MAP = json.loads(os.environ.get('S3_KEY_MAP', '{}'))


def handler(event, context):
    try:
        # 1. Identity & Request Validation
        user_id = event['requestContext']['authorizer']['claims']['sub']
        params = event.get('queryStringParameters') or {}
        product_id = params.get('productId')

        if not product_id:
            return {'statusCode': 400, 'body': json.dumps({'error': 'Missing productId'})}

        # 2. Look up the specific S3 path for this product
        file_key = S3_KEY_MAP.get(product_id)
        if not file_key:
            return {'statusCode': 404, 'body': json.dumps({'error': 'Product file path not configured'})}

        # 3. Security Check: Did they actually buy it?
        table = dynamodb.Table(TABLE_NAME)
        response = table.get_item(
            Key={'userId': user_id, 'productId': product_id})

        if 'Item' not in response:
            return {
                'statusCode': 403,
                'headers': {"Access-Control-Allow-Origin": "*"},
                'body': json.dumps({'error': 'No valid purchase found for this product'})
            }

        # 4. Generate Presigned URL
        # We use the key from our map, NOT the raw product_id
        presigned_url = s3.generate_presigned_url(
            'get_object',
            Params={'Bucket': BUCKET_NAME, 'Key': file_key},
            ExpiresIn=900  # 15 minutes
        )

        return {
            'statusCode': 200,
            'headers': {
                "Access-Control-Allow-Origin": "*",
                "Content-Type": "application/json"
            },
            'body': json.dumps({'downloadUrl': presigned_url})
        }

    except Exception as e:
        print(f"Download Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'error': 'Internal server error'})
        }
