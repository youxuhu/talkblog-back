from fastapi import FastAPI
from pydantic import BaseModel
from fastapi import Header, Cookie
from fastapi.responses import RedirectResponse
from fastapi import HTTPException
import logging
logger = logging.getLogger("uvicorn.error")

from fastapi.responses import JSONResponse
from app.api.llm_helper import router as llm_router

app = FastAPI()
app.include_router(llm_router, prefix="/api/ai", tags=["llm"])


class Item(BaseModel):
    name: str
    description: str = None
    price: float
    tax: float = None




@app.get("/")
async def root()-> dict:
    return {"message": "Hello World"}

# @app.post("/items/{item_id}")
# async def read_item(item_id: int, q: str | None = None)-> dict:
#     return {"item_id": item_id, "q": q}
# def create_item(item: Item)-> Item:
#     return item
# @app.get("/items/")
# def read_items(user_agent: str = Header(None), session_token: str = Cookie(None))-> dict:
#     return {"User-Agent": user_agent, "session_token": session_token}

# @app.get("/redirect")
# def redirect()-> RedirectResponse:
#     return RedirectResponse(url="/items/")

# @app.get("/items/{item_id}")
# def read_items(item_id: int)-> dict:
#     if item_id == 42:
#         raise HTTPException(status_code=404, detail="Item not found")
#     return {"item_id": item_id}



@app.get("/items/{item_id}")
def read_items(item_id: int):
    content = {"item_id": item_id}
    header = {"X-Custom-Header": "Custom Value"}
    return JSONResponse(content=content, headers=header)