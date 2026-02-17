import json
import boto3
import os

# Initialize clients outside the handler for better performance
dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')

# Get environment variables set by Terraform
TABLE_NAME = os.environ.get('TABLE_NAME')
BUCKET_NAME = os.environ.get('VST_BUCKET_NAME')


def lambda_handler(event, context):
    # 1. Get User ID from Cognito Context
    user_id = event['requestContext']['authorizer']['claims']['sub']

    # In a real app, you'd get this from event['queryStringParameters']['productId']
    product_id = "cool-synth-v1"

    try:
        table = dynamodb.Table(TABLE_NAME)

        # 2. Check DynamoDB for a purchase record
        # CRITICAL: Keys must match your Terraform (userId / productId)
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
                'body': json.dumps({'message': 'Access Denied: Product not purchased.'})
            }

        # 3. If they paid, generate the S3 URL
        url = s3.generate_presigned_url(
            'get_object',
            Params={'Bucket': BUCKET_NAME, 'Key': 'cool-synth-plugin.zip'},
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
