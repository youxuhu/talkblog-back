from .get_config import LLMConfig
from ..pojo.llm_message import llm_msg
from zhipuai import ZhipuAI


class LLMService:
    def __init__(self):
        self.config = LLMConfig().config
        self.openai_config = self.config.get("openai", {})
        self.request_config = self.config.get("ask", {})

    def _get_model(self) -> str:
        return self.openai_config.get("model")

    def _get_max_tokens(self) -> int:
        return self.request_config.get("max_tokens")

    def _get_temperature(self) -> float:
        return self.request_config.get("temperature")

    def _is_modification_request(self, question: str) -> bool:
        keywords = ['润色', '纠正', '缩短', '扩写', '优化', '修改', '改写', '重写', '生成', '写一篇', '写一段', 'polish', 'rewrite', 'correct', 'shorten', 'expand', 'generate']
        return any(k in question for k in keywords)

    def _build_messages(self, blog_title: str, blog_content: str, question: str, selected_text: str = None) -> list[dict]:
        if selected_text:
            system_prompt = "你是一个中文写作助手。用户选中了一段文本，请根据用户要求直接修改选中文本，只返回修改后的结果，不要任何解释或额外内容。"
            user_prompt = (
                f"博客标题：{blog_title}\n"
                f"博客全文上下文：\n{blog_content}\n\n"
                f"用户选中的文本：{selected_text}\n"
                f"用户要求：{question}\n\n"
                "请直接返回修改后的文本，不要添加任何解释。"
            )
        elif self._is_modification_request(question):
            system_prompt = "你是一个纯文本处理工具，直接输出处理后的结果。禁止输出任何前缀、后缀、标签或标记，禁止出现'标题'、'内容'、'正文'、'修改后'等说明文字，禁止使用**加粗**或任何格式标记。只输出纯文本，一个字都不要多。"
            user_prompt = (
                f"博客标题：{blog_title}\n"
                f"博客内容：\n{blog_content}\n\n"
                f"用户要求：{question}\n\n"
                "只输出纯文本结果，不要多余的任何字。不要说明、不要解释、不要前缀。"
            )
        else:
            system_prompt = "你是一个中文写作助手。请结合博客内容，针对用户问题给出简洁、准确、可执行的建议。"
            user_prompt = (
                f"博客标题：{blog_title}\n"
                f"博客内容：\n{blog_content}\n\n"
                f"用户问题：{question}\n"
                "请直接回答用户问题，并给出必要的修改建议，回答不超过500字。"
            )

        return [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ]

    def get_response(self, blog_title: str, blog_content: str, question: str, selected_text: str = None) -> llm_msg:
        client = ZhipuAI(api_key=self.openai_config.get("api_key"))

        resp = client.chat.completions.create(
            model=self._get_model(),
            messages=self._build_messages(blog_title, blog_content, question, selected_text),
            thinking={"type": "enabled"},
            max_tokens=self._get_max_tokens(),
            temperature=self._get_temperature(),
        )

        content = ""
        choices = resp.choices
        if choices:
            content = choices[0].message.content

        return llm_msg(content)
