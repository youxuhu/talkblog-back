from pydantic import BaseModel

class ResponseSchema(BaseModel):
    response: str
    type: int