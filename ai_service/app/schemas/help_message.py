from pydantic import BaseModel
from typing import Optional

class HelpMessageSchema(BaseModel):
    blog_title: str
    blog_content: str
    question: str
    selected_text: Optional[str] = None
