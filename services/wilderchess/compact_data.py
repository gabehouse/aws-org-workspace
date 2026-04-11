import pandas as pd
import boto3
import os
import time
from concurrent.futures import ThreadPoolExecutor

BUCKET_NAME = "wilderchess-training-data-dev-c10cadc6"
LOCAL_TEMP_DIR = "temp_chunks"
INTERMEDIATE_DIR = "intermediate_parquet"


def download_and_read(s3_client, bucket, key):
    local_path = os.path.join(LOCAL_TEMP_DIR, os.path.basename(key))
    try:
        s3_client.download_file(bucket, key, local_path)
        df = pd.read_csv(local_path)
        os.remove(local_path)
        return df
    except Exception as e:
        print(f"⚠️ Error downloading {key}: {e}")
        return None


def list_all_keys(bucket, prefix):
    s3 = boto3.client('s3')
    keys = []
    continuation_token = None
    while True:
        list_kwargs = {'Bucket': bucket, 'Prefix': prefix}
        if continuation_token:
            list_kwargs['ContinuationToken'] = continuation_token
        response = s3.list_objects_v2(**list_kwargs)
        if 'Contents' in response:
            keys.extend([obj['Key'] for obj in response['Contents']
                        if obj['Key'].endswith('.csv')])
        if not response.get('IsTruncated'):
            break
        continuation_token = response.get('NextContinuationToken')
    return keys


def compact_s3_data():
    # 1. Setup
    for d in [LOCAL_TEMP_DIR, INTERMEDIATE_DIR]:
        if not os.path.exists(d):
            os.makedirs(d)

    s3 = boto3.client('s3')
    all_keys = list_all_keys(BUCKET_NAME, "staging/")
    chunk_size = 1000
    print(f"📦 Total files to process: {len(all_keys)}")

    # 2. Process Chunks
    for i in range(0, len(all_keys), chunk_size):
        chunk_file = f"{INTERMEDIATE_DIR}/chunk_{i}.parquet"

        if os.path.exists(chunk_file):
            print(f"⏭️ Skipping chunk {i//chunk_size + 1} (Already exists)")
            continue

        chunk = all_keys[i:i + chunk_size]
        print(
            f"🔄 Processing chunk {i//chunk_size + 1} ({i} to {i+len(chunk)})...")

        with ThreadPoolExecutor(max_workers=8) as executor:
            results = list(executor.map(
                lambda k: download_and_read(s3, BUCKET_NAME, k), chunk))

        # Filter Nones and save this chunk to disk immediately
        valid_dfs = [df for df in results if df is not None]
        if valid_dfs:
            pd.concat(valid_dfs, ignore_index=True).to_parquet(chunk_file)

        # Give the network stack 1 second to breathe between 1k downloads
        time.sleep(1)


BUCKET_NAME = "wilderchess-training-data-dev-c10cadc6"
LOCAL_TEMP_DIR = "temp_chunks"
INTERMEDIATE_DIR = "intermediate_parquet"


def download_and_read(s3_client, bucket, key):
    local_path = os.path.join(LOCAL_TEMP_DIR, os.path.basename(key))
    try:
        s3_client.download_file(bucket, key, local_path)
        df = pd.read_csv(local_path)
        os.remove(local_path)
        return df
    except Exception as e:
        print(f"⚠️ Error downloading {key}: {e}")
        return None


def list_all_keys(bucket, prefix):
    s3 = boto3.client('s3')
    keys = []
    continuation_token = None
    while True:
        list_kwargs = {'Bucket': bucket, 'Prefix': prefix}
        if continuation_token:
            list_kwargs['ContinuationToken'] = continuation_token
        response = s3.list_objects_v2(**list_kwargs)
        if 'Contents' in response:
            keys.extend([obj['Key'] for obj in response['Contents']
                        if obj['Key'].endswith('.csv')])
        if not response.get('IsTruncated'):
            break
        continuation_token = response.get('NextContinuationToken')
    return keys


def compact_s3_data():
    # 1. Setup
    for d in [LOCAL_TEMP_DIR, INTERMEDIATE_DIR]:
        if not os.path.exists(d):
            os.makedirs(d)

    s3 = boto3.client('s3')
    all_keys = list_all_keys(BUCKET_NAME, "staging/")
    chunk_size = 1000
    print(f"📦 Total files to process: {len(all_keys)}")

    # 2. Process Chunks
    for i in range(0, len(all_keys), chunk_size):
        chunk_file = f"{INTERMEDIATE_DIR}/chunk_{i}.parquet"

        if os.path.exists(chunk_file):
            print(f"⏭️ Skipping chunk {i//chunk_size + 1} (Already exists)")
            continue

        chunk = all_keys[i:i + chunk_size]
        print(
            f"🔄 Processing chunk {i//chunk_size + 1} ({i} to {i+len(chunk)})...")

        with ThreadPoolExecutor(max_workers=8) as executor:
            results = list(executor.map(
                lambda k: download_and_read(s3, BUCKET_NAME, k), chunk))

        # Filter Nones and save this chunk to disk immediately
        valid_dfs = [df for df in results if df is not None]
        if valid_dfs:
            pd.concat(valid_dfs, ignore_index=True).to_parquet(chunk_file)

        # Give the network stack 1 second to breathe between 1k downloads
        time.sleep(1)

# 3. Final Merge (The "Stream-to-Disk" approach)
    print("🧹 Finalizing master file incrementally...")
    parquet_files = sorted([f for f in os.listdir(
        INTERMEDIATE_DIR) if f.endswith('.parquet')])

    if not parquet_files:
        print("❌ No data found to merge.")
        return

    output_file = "master_data.parquet"

    # We use fastparquet because it handles 'append' very efficiently on disk
    for i, file_name in enumerate(parquet_files):
        file_path = os.path.join(INTERMEDIATE_DIR, file_name)
        df_chunk = pd.read_parquet(file_path)

        # Write to disk. append=True for every file except the first one.
        df_chunk.to_parquet(output_file, engine='fastparquet', append=(i > 0))

        print(
            f"✅ Appended {file_name} to master... ({i+1}/{len(parquet_files)})")

    print(f"🚀 Success! Master file '{output_file}' is ready.")


if __name__ == "__main__":
    compact_s3_data()
