import cv2
from flask import Flask, request, jsonify
import base64
import binascii
import numpy as np
from insightface.app import FaceAnalysis
import onnxruntime as ort

app = Flask(__name__)
# 限制请求体大小，避免超大图片请求占满内存
app.config['MAX_CONTENT_LENGTH'] = 8 * 1024 * 1024

# 初始化 InsigtFace 模型
# 将在./models/目录下加载模型文件
available_providers = ort.get_available_providers()
providers = ['CPUExecutionProvider']
face_app = FaceAnalysis(name='buffalo_l', root='./models/', providers=providers)

# 强制使用 CPU，ctx_id 需为 -1
face_app.prepare(ctx_id=-1, det_size=(640, 640))

def get_active_providers():
    # 从已加载模型的 ORT Session 读取实际生效的 provider
    for model in face_app.models.values():
        session = getattr(model, 'session', None)
        if session is not None:
            return session.get_providers()
    return ['Unknown']

print("模型加载完成")
active_providers = get_active_providers()
print(f"可用 Providers: {available_providers}")
print(f"当前 Providers: {active_providers}")

def base64_to_cv2(base64_str):
    if not isinstance(base64_str, str) or not base64_str.strip():
        return None
    if ',' in base64_str:
        base64_str = base64_str.split(',')[1]
    try:
        # validate=True 可直接拦截非法 base64 输入
        img_data = base64.b64decode(base64_str, validate=True)
    except (ValueError, binascii.Error):
        return None
    np_arr = np.frombuffer(img_data, np.uint8)
    return cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

@app.route('/health', methods=['GET'])
def health():
    """健康检查"""
    active_providers = get_active_providers()
    return jsonify({
        "status": "healthy",
        "backend": "GPU" if 'CUDAExecutionProvider' in active_providers else "CPU",
        "providers": active_providers
    })


@app.route('/vectorize', methods=['POST'])
@app.route('/extract', methods=['POST'])
def extract_face_vector():
    """提取人脸向量（提供给 Spring Boot 调用）"""
    # silent=True 避免无效 JSON 触发异常
    data = request.get_json(silent=True)
    if not isinstance(data, dict):
        return jsonify({"success": False, "error": "Invalid JSON body"}), 400

    img_b64 = data.get('image')
    
    if not img_b64:
        return jsonify({"success": False, "error": "Missing 'image' field in request"}), 400
    
    img = base64_to_cv2(img_b64)
    if img is None:
        return jsonify({"success":False, "error": "Invalid image data"}), 400
    
    # 人脸检测和特征提取
    try:
        # 模型推理失败时返回可控错误，避免接口直接 500 崩溃
        faces = face_app.get(img)
    except Exception:
        return jsonify({"success": False, "error": "Face extraction failed"}), 500

    if len(faces) == 0:
        return jsonify({"success": False, "error": "No face detected"}), 400
    if len(faces) > 1:
        return jsonify({"success": False, "error": "Multiple faces detected, provide a single face image"}), 400
    
    face = faces[0]  # 只处理第一张人脸
    embedding = face.normed_embedding.tolist()
    bbox = face.bbox.astype(int).tolist()
    
    return jsonify({
        "success": True,
        "face_vector": embedding,
        "bbox": bbox,
        "vector_dim": len(embedding)
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
