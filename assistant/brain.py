"""Small rule-based conversation brain; designed to be replaceable by a local model later."""
import re

class Brain:
    def __init__(self, owner="Biswajit", name="JARVIS"):
        self.owner = owner
        self.name = name
        self.quiet = False

    def reply(self, text: str):
        t = text.strip().lower()
        if not t:
            return None
        if any(x in t for x in ["চুপ থাক", "চুপ কর", "chup thak", "chup koro", "be quiet", "stay quiet"]):
            self.quiet = True
            return "ঠিক আছে। আমি চুপ থাকছি। আবার কথা বলতে বললেই বলব।"
        if any(x in t for x in ["আবার কথা বল", "abar kotha bolo", "talk again", "speak again"]):
            self.quiet = False
            return "ঠিক আছে। আমি আবার active থাকলাম।"
        if "তোমার নাম" in t or "your name" in t or "naam ki" in t:
            return f"আমার নাম {self.name}।"
        if "আমাকে কে বানিয়েছে" in t or "who made you" in t or "ke baniyeche" in t:
            return f"আমাকে {self.owner} বানাচ্ছে।"
        if "কেমন আছ" in t or "how are you" in t:
            return "আমি ঠিক আছি। তোমার সঙ্গেই আছি।"
        if "সময়" in t or "time" in t:
            return None
        if re.search(r"\b(hi|hello|hey)\b", t):
            return "হ্যাঁ, বলো। আমি শুনছি।"
        return f"হুম, শুনছি। তুমি বললে আমি সেটা বুঝে উত্তর দেওয়ার চেষ্টা করব।"
