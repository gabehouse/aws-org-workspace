import json
import boto3
import os

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table(os.environ['TABLE_NAME'])


def lambda_handler(event, context):
    # This logic would eventually check if a user owns a VST
    user_email = event['requestContext']['authorizer']['claims']['email']

    return {
        'statusCode': 200,
        'body': json.dumps({
            'message': f"Hello {user_email}, welcome to the VST shop!",
            'status': 'authorized'
        })
    }
