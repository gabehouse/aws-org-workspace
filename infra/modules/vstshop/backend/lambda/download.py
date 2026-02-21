import json
import os
import boto3
from botocore.exceptions import ClientError

dynamodb = boto3.resource('dynamodb')
s3 = boto3.client('s3')

TABLE_NAME = os.environ['TABLE_NAME']
BUCKET_NAME = os.environ['STORAGE_BUCKET']


def handler(event, context):
    try:
        # 1. Get User ID from Cognito (Authorizer)
        user_id = event['requestContext']['authorizer']['claims']['sub']

        # 2. Get Product ID from Query Parameters
        product_id = event.get('queryStringParameters', {}).get('productId')

        if not product_id:
            return {'statusCode': 400, 'body': json.dumps({'error': 'Missing productId'})}

        # 3. Check DynamoDB for purchase
        table = dynamodb.Table(TABLE_NAME)
        response = table.get_item(
            Key={'userId': user_id, 'productId': product_id})

        if 'Item' not in response:
            return {'statusCode': 403, 'body': json.dumps({'error': 'Purchase not found'})}

        # 4. Generate Presigned URL (Valid for 15 mins)
        file_key = f"{product_id}.zip"
        presigned_url = s3.generate_presigned_url(
            'get_object',
            Params={'Bucket': BUCKET_NAME, 'Key': file_key},
            ExpiresIn=900
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
        print(f"Error: {str(e)}")
        return {'statusCode': 500, 'body': json.dumps({'error': 'Internal server error'})}
