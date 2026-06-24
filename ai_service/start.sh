#!/bin/bash
cd "$(dirname "$0")"
PID_FILE="/tmp/ai_service.pid"

if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "AI 服务已在运行 (PID: $OLD_PID)"
        exit 0
    fi
fi

# 清理可能残留的旧进程（避免匹配自身）
pkill -f "python3 run.py" 2>/dev/null || true
sleep 1

setsid /home/lenovo/miniconda3/bin/python3 run.py > /tmp/ai_service.log 2>&1 &
PID=$!
echo $PID > "$PID_FILE"
sleep 2
if kill -0 "$PID" 2>/dev/null; then
    echo "AI 服务已启动 (PID: $PID)"
    echo "查看日志: tail -f /tmp/ai_service.log"
else
    echo "启动失败，查看日志: cat /tmp/ai_service.log"
    rm -f "$PID_FILE"
fi
