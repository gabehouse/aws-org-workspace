import json
import boto3
import os
from botocore.config import Config

# Initialize S3 client with Signature Version 4 (required for modern regions)
s3_client = boto3.client(
    's3',
    region_name=os.environ['AWS_REGION'],
    config=Config(signature_version='s3v4')
)


def lambda_handler(event, context):
    try:
        user_email = event['requestContext']['authorizer']['claims']['email']
        bucket_name = os.environ['VST_BUCKET_NAME']
        # This key would eventually be dynamic based on the VST requested
        object_key = "cool-synth-plugin.zip"

        # Generate the temporary download link
        presigned_url = s3_client.generate_presigned_url(
            'get_object',
            Params={'Bucket': bucket_name, 'Key': object_key},
            ExpiresIn=300  # Link valid for 5 minutes
        )

        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type,Authorization',
                'Access-Control-Allow-Methods': 'GET,OPTIONS'
            },
            'body': json.dumps({
                'message': f"Authorized for {user_email}",
                'downloadUrl': presigned_url
            })
        }

    except Exception as e:
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {'Access-Control-Allow-Origin': '*'},
            'body': json.dumps({'error': 'Could not generate download link'})
        }
