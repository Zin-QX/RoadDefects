from __future__ import annotations
import os
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
os.environ["YOLO_CONFIG_DIR"] = os.path.join(BASE_DIR, "yolo_config")
sys.path.insert(0, BASE_DIR)

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import logging

from image_utils import download_image, cleanup_file
from model_utils import process_image
from cos_utils import upload_to_cos

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Road Defect Detection API",
    description="API for detecting road defects in images using YOLO model",
    version="1.0.0",
)

CLASS_ID_TO_CN = {
    0: "裂缝",
    1: "井盖",
    2: "网裂",
    3: "坑洞",
    4: "修补裂缝",
    5: "修补网裂",
    6: "修补坑洞",
}


class DetectionRequest(BaseModel):
    id: int
    url: str
    userId: int


class DetectionResult(BaseModel):
    id: int
    processedUrl: str
    processedResult: str


@app.post("/detect", response_model=DetectionResult)
async def detect_defects(request: DetectionRequest):
    input_path = None
    output_path = None
    
    try:
        logger.info(f"Processing request id={request.id}, url={request.url}")
        
        input_path = download_image(request.url)
        logger.info(f"Image downloaded to {input_path}")
        
        output_path, detections = process_image(input_path)
        logger.info(f"Model processing complete, found {len(detections)} detections")
        
        image_url = upload_to_cos(output_path, user_id=request.userId)
        logger.info(f"Result uploaded to {image_url}")
        
        if detections:
            result_parts = []
            for det in detections:
                class_cn = CLASS_ID_TO_CN.get(det['class_id'], det['class'])
                # result_parts.append(f"{class_cn}(置信度:{det['confidence']:.2%})")
                result_parts.append(f"{class_cn}")
            import json
            processed_result = json.dumps(result_parts, ensure_ascii=False)
        else:
            processed_result = "[]"
        
        return DetectionResult(
            id=request.id,
            processedUrl=image_url,
            processedResult=processed_result,
        )
    
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    
    finally:
        if input_path:
            cleanup_file(input_path)
        if output_path:
            cleanup_file(output_path)


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
