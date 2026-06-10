import uvicorn
import logging

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, timeout_keep_alive=300, log_level="info")
