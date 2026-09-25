from dataclasses import dataclass

@dataclass
class Config:
    name: str = "JARVIS"
    owner: str = "Biswajit"
    language: str = "bn-IN"
    silence_seconds: int = 90
    proactive_cooldown_seconds: int = 300
    quiet_mode: bool = False
