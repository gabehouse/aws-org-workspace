import json
import boto3
import os

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table(os.environ['TABLE_NAME'])


def lambda_handler(event, context):
    # Log the event for debugging (view in CloudWatch)
    print(f"Received event: {json.dumps(event)}")

    try:
        # Extract user email from Cognito claims
        # In a proxy integration, this is nested under requestContext
        user_email = event['requestContext']['authorizer']['claims']['email']

        response_body = {
            'message': f"Hello {user_email}, welcome to the VST shop!",
            'status': 'authorized'
        }

        return {
            'statusCode': 200,
            'headers': {
                # For dev; change to your CloudFront URL for prod
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type,Authorization,X-Amz-Date,X-Api-Key,X-Amz-Security-Token',
                'Access-Control-Allow-Methods': 'GET,OPTIONS'
            },
            'body': json.dumps(response_body)
        }

    except Exception as e:
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
            },
            'body': json.dumps({'error': 'Internal Server Error'})
        }
