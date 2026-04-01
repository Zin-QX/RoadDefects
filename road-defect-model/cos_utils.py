import os
import uuid
from qcloud_cos import CosConfig
from qcloud_cos import CosS3Client
from config import COS_CONFIG


def get_cos_client() -> CosS3Client:
    config = CosConfig(
        Region=COS_CONFIG["region"],
        SecretId=COS_CONFIG["secret_id"],
        SecretKey=COS_CONFIG["secret_key"],
    )
    return CosS3Client(config)


def upload_to_cos(local_path: str, user_id: int = None, remote_path: str = None) -> str:
    client = get_cos_client()
    
    if remote_path is None:
        file_ext = os.path.splitext(local_path)[1]
        if user_id is not None:
            remote_path = f"road-defects/{user_id}/{uuid.uuid4().hex}{file_ext}"
        else:
            remote_path = f"road-defects/{uuid.uuid4().hex}{file_ext}"
    
    with open(local_path, "rb") as fp:
        client.put_object(
            Bucket=COS_CONFIG["bucket"],
            Body=fp,
            Key=remote_path,
        )
    
    return f"{COS_CONFIG['host']}/{remote_path}"
