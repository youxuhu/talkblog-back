from fastapi import APIRouter, HTTPException
from app.schemas.help_message import HelpMessageSchema
from app.schemas.help_response import ResponseSchema
from app.service.llm_service import LLMService

router = APIRouter()

@router.post("/help")
def get_help(msg: HelpMessageSchema) -> ResponseSchema:
    try:
        llm_service = LLMService()
        result = llm_service.get_response(
            blog_title=msg.blog_title,
            blog_content=msg.blog_content,
            question=msg.question,
            selected_text=msg.selected_text,
        )
        return ResponseSchema(response=result.message, type=1)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))