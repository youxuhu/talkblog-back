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

nohup python3 run.py > /tmp/ai_service.log 2>&1 &
echo $! > "$PID_FILE"
echo "AI 服务已启动 (PID: $!)"
echo "查看日志: tail -f /tmp/ai_service.log"
