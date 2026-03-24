import os
import uuid
import requests
from config import CACHE_DIR


def download_image(url: str) -> str:
    response = requests.get(url, timeout=30)
    response.raise_for_status()
    
    content_type = response.headers.get("content-type", "")
    ext = ".jpg"
    if "png" in content_type:
        ext = ".png"
    elif "jpeg" in content_type or "jpg" in content_type:
        ext = ".jpg"
    
    filename = f"{uuid.uuid4().hex}{ext}"
    filepath = os.path.join(CACHE_DIR, filename)
    
    with open(filepath, "wb") as f:
        f.write(response.content)
    
    return filepath


def cleanup_file(filepath: str):
    if os.path.exists(filepath):
        os.remove(filepath)
