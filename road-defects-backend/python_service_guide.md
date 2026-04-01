# Python 图片处理接口规范

## 接口说明

Java 后端在用户上传完成后会调用 Python 图片处理接口，将图片 ID 和图片 URL 发送给 Python 服务进行处理。

## 接口定义

### 请求方式
`POST`

### 请求 URL
`http://localhost:5000/detect`

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 图片 ID |
| url | String | 是 | 图片 URL（COS 地址） |
| userId | Long | 是 | 用户 ID，用于区分用户文件 |

### 请求示例

```json
{
  "id": 2034196409431478274,
  "url": "https://zinqx-1391992760.cos.ap-guangzhou.myqcloud.com/RoadDefects/2026-03-18_2PHlIeaFzhoqn0QT.jpg",
  "userId": 1234567890
}
```

### 响应格式

Python 接口直接返回处理结果，不需要 `code` 和 `data` 包装。

#### 成功响应示例

```json
{
  "id": 2034196409431478300,
  "processedUrl": "https://zinqx-1391992760.cos.ap-guangzhou.myqcloud.com/road-defects/ef01d1671c204a8ead813a9f24a39dff.jpg",
  "processedResult": "[\"修补裂缝(置信度:76.64%)\", \"修补网裂(置信度:39.26%)\"]"
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 图片 ID（可选，用于日志记录） |
| processedUrl | String | **必填**，处理后的图片 URL（标注了缺陷位置的图片） |
| processedResult | String | **必填**，处理结果描述文本，**JSON 字符串格式的列表**，如：`["缺陷1(置信度:xx%)", "缺陷2(置信度:xx%)"]` |

**注意**：`processedResult` 字段必须是 JSON 字符串格式的列表，Java 后端会将其解析为 `List<String>` 返回给前端。

## Python 服务实现建议

### 1. 使用 Flask 框架

```python
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

@app.route('/detect', methods=['POST'])
def detect():
    # 获取 JSON 请求参数
    data = request.get_json()
    picture_id = data.get('id')
    picture_url = data.get('url')
    user_id = data.get('userId')
    
    if not picture_id or not picture_url or not user_id:
        return jsonify({
            "error": "缺少必要参数：id、url 和 userId"
        }), 400
    
    try:
        # 1. 下载图片
        # 2. 调用 AI 模型进行处理
        # 3. 根据 user_id 上传处理后的图片到对应用户目录
        # 4. 返回处理结果
        
        return jsonify({
            "id": picture_id,
            "processedUrl": "https://xxx.cos.ap-guangzhou.myqcloud.com/processed.jpg",
            "processedResult": "共检测到 2 个缺陷：Patch-Pothole(置信度:45.73%), Manhole(置信度:38.75%)"
        })
    except Exception as e:
        return jsonify({
            "error": f"图片处理失败：{str(e)}"
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

### 2. 处理流程

1. **接收参数**：从请求中获取 pictureId、pictureUrl 和 userId
2. **下载图片**：根据 pictureUrl 从 COS 下载原始图片
3. **AI 处理**：调用深度学习模型进行裂缝检测
4. **上传结果**：根据 userId 将处理后的图片上传到对应用户目录（例如：`RoadDefects/{userId}/processed/`）
5. **返回结果**：返回处理后的图片 URL 和处理结果

### 3. 注意事项

- Python 服务需要保持运行状态
- 建议设置合理的超时时间（默认 5 秒）
- **必须返回 `processedUrl` 字段**，否则 Java 后端会认为处理失败
- `processedResult` 字段可以是任意描述性文本，不一定是 JSON 格式
- 如果处理失败，可以返回空的 `processedUrl` 或者不返回该字段

## 配置说明

在 `application.yml` 中配置 Python 服务地址：

```yaml
python:
  service:
    base-url: http://localhost:5000
    process-picture-endpoint: /process/picture
```

生产环境请修改为实际的 Python 服务部署地址。

## 错误处理

Java 后端会捕获 Python 服务调用异常，如果调用失败：
- 不会中断图片上传流程
- 会在日志中记录错误信息
- 数据库中 processedUrl 和 processedResult 字段保持为空
