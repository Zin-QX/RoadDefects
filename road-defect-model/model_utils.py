from __future__ import annotations
import os
import uuid
from ultralytics import YOLO
from config import MODEL_PATH, CACHE_DIR


_model = None


def get_model() -> YOLO:
    global _model
    if _model is None:
        _model = YOLO(MODEL_PATH)
    return _model


def process_image(image_path: str) -> tuple[str, list[dict]]:
    model = get_model()
    
    results = model(image_path)
    
    detections = []
    for result in results:
        boxes = result.boxes
        if boxes is not None:
            for i in range(len(boxes)):
                box = boxes.xyxy[i].cpu().numpy()
                conf = float(boxes.conf[i].cpu().numpy())
                cls = int(boxes.cls[i].cpu().numpy())
                cls_name = result.names[cls]
                
                detections.append({
                    "class": cls_name,
                    "class_id": cls,
                    "confidence": round(conf, 4),
                    "bbox": {
                        "x1": float(box[0]),
                        "y1": float(box[1]),
                        "x2": float(box[2]),
                        "y2": float(box[3]),
                    }
                })
    
    output_filename = f"{uuid.uuid4().hex}.jpg"
    output_path = os.path.join(CACHE_DIR, output_filename)
    
    if results and len(results) > 0:
        results[0].save(output_path)
    else:
        import shutil
        shutil.copy(image_path, output_path)
    
    return output_path, detections
