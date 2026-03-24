import os

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

MODEL_PATH = os.path.join(BASE_DIR, "best.pt")

COS_CONFIG = {
    "host": "https://zinqx-1391992760.cos.ap-guangzhou.myqcloud.com",
    "secret_id": "AKIDIRP8uVu66uWzKUTM2WwBE5MVSDmFXeZH",
    "secret_key": "b0CG7mJleMsCMK5eKsGVaUCnDRTJxRo2",
    "region": "ap-guangzhou",
    "bucket": "zinqx-1391992760",
}

CACHE_DIR = os.path.join(BASE_DIR, "cache")
os.makedirs(CACHE_DIR, exist_ok=True)
