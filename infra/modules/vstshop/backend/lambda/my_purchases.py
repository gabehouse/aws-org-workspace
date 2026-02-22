import os
import json
import boto3
from boto3.dynamodb.conditions import Key

dynamodb = boto3.resource('dynamodb')
TABLE_NAME = os.environ.get('TABLE_NAME')


def handler(event, context):
    try:
        # 1. Identify who is asking (from Cognito)
        user_id = event['requestContext']['authorizer']['claims']['sub']
        table = dynamodb.Table(TABLE_NAME)

        # 2. Query DynamoDB for ALL items belonging to this user
        # This is better than get_item because it returns a list
        response = table.query(
            KeyConditionExpression=Key('userId').eq(user_id)
        )

        # 3. Simplify the data for React
        # Turns DynamoDB rows into: ["cool-synth-v1", "retro-verb-v1"]
        owned_ids = [item['productId'] for item in response.get('Items', [])]

        return {
            'statusCode': 200,
            'headers': {
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Headers": "Content-Type,Authorization",
                "Access-Control-Allow-Methods": "GET,OPTIONS"
            },
            'body': json.dumps(owned_ids)
        }
    except Exception as e:
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'error': str(e)})
        }
