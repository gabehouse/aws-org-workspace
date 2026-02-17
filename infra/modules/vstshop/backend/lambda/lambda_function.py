import json
import boto3
import os

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('vstshop-purchases')
s3 = boto3.client('s3')


def lambda_handler(event, context):
    # 1. Get User ID from Cognito Context
    user_id = event['requestContext']['authorizer']['claims']['sub']
    # Hardcoded for now, but eventually this comes from the frontend
    product_id = "cool-synth-v1"

    try:
        # 2. Check DynamoDB for a purchase record
        response = table.get_item(
            Key={
                'user_id': user_id,
                'product_id': product_id
            }
        )

        if 'Item' not in response:
            return {
                'statusCode': 403,
                'headers': {"Access-Control-Allow-Origin": "*"},
                'body': json.dumps({'message': 'Access Denied: Product not purchased.'})
            }

        # 3. If they paid, generate the S3 URL
        url = s3.generate_presigned_url(
            'get_object',
            Params={'Bucket': os.environ['BUCKET_NAME'],
                    'Key': 'cool-synth-plugin.zip'},
            ExpiresIn=3600
        )

        return {
            'statusCode': 200,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'downloadUrl': url})
        }

    except Exception as e:
        print(e)
        return {'statusCode': 500, 'body': json.dumps({'message': 'Internal Server Error'})}
