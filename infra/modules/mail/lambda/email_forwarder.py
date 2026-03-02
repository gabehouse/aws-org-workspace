import boto3
import os
import email
from email.header import decode_header

s3 = boto3.client('s3')
ses = boto3.client('ses')


def decode_mime_words(s):
    """Helper to turn encoded subjects into readable text."""
    return "".join(
        word.decode(encoding or "utf-8") if isinstance(word, bytes) else word
        for word, encoding in decode_header(s)
    )


def handler(event, context):
    bucket = os.environ['BUCKET_NAME']
    key = event['Records'][0]['ses']['mail']['messageId']

    # 1. Get the raw email from S3
    response = s3.get_object(Bucket=bucket, Key=key)
    raw_email_data = response['Body'].read()

    # 2. Parse the email
    msg = email.message_from_bytes(raw_email_data)

    # 3. CAPTURE values BEFORE deleting
    # We decode the subject in case it has emojis or special chars
    raw_subject = msg.get('Subject', 'No Subject')
    original_subject = decode_mime_words(raw_subject)
    original_sender = str(msg.get('From', 'Unknown Sender'))

    # 4. DELETE old headers to avoid "Duplicate Header" errors
    del msg['Subject']
    del msg['From']
    del msg['To']
    del msg['Return-Path']

    # 5. REBUILD fresh headers
    msg['Subject'] = f"Support: {original_subject}"
    msg['From'] = f"support@houseaudio.net"
    msg['To'] = os.environ['PERSONAL_EMAIL']
    msg['Reply-To'] = original_sender

    # 6. THE MISSING PIECE: Send it!
    try:
        ses.send_raw_email(
            Source=msg['From'],
            Destinations=[msg['To']],
            RawMessage={'Data': msg.as_string()}
        )
        print(f"Successfully forwarded email from {original_sender}")
    except Exception as e:
        print(f"Error forwarding email: {e}")
        raise e
