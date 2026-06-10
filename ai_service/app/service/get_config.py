from dotenv import load_dotenv
from pathlib import Path

import yaml

class LLMConfig:
    def __init__(self, config_path: str | Path | None = None):
        load_dotenv()
        self.config_path = Path(config_path) if config_path is not None else self._get_project_root() / "config.yaml"
        self.config = self._load_config()
    
    def _get_project_root(self):
        return Path(__file__).resolve().parent.parent.parent

    def _load_config(self):
        if not self.config_path.exists():
            raise FileNotFoundError(f"Config file not found at {self.config_path}")
        
        with self.config_path.open("r", encoding="utf-8") as file:
            config = yaml.safe_load(file) or {}

        if not isinstance(config, dict):
            raise ValueError(f"Config file must contain a mapping at the top level: {self.config_path}")

        return config