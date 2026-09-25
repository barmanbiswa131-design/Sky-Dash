import time

class Proactive:
    def __init__(self, silence_seconds=90, cooldown_seconds=300):
        self.silence_seconds = silence_seconds
        self.cooldown_seconds = cooldown_seconds
        self.last_user_activity = time.monotonic()
        self.last_prompt = 0.0

    def user_spoke(self):
        self.last_user_activity = time.monotonic()

    def silence_prompt(self, quiet=False):
        now = time.monotonic()
        if quiet or now - self.last_user_activity < self.silence_seconds:
            return None
        if now - self.last_prompt < self.cooldown_seconds:
            return None
        self.last_prompt = now
        return "কী হলো? অনেকক্ষণ কিছু বলছো না। সব ঠিক আছে তো?"
