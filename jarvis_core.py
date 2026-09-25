import re, time, shutil, subprocess

class Brain:
    def __init__(self): self.quiet=False
    def reply(self,text):
        t=text.strip().lower()
        if not t:return None
        if any(x in t for x in ["চুপ থাক","চুপ কর","chup thak","chup koro","be quiet","stay quiet"]): self.quiet=True; return "ঠিক আছে। আমি চুপ থাকছি। আবার কথা বলতে বললেই বলব।"
        if any(x in t for x in ["আবার কথা বল","abar kotha bolo","talk again","speak again"]): self.quiet=False; return "ঠিক আছে। আমি আবার active থাকলাম।"
        if "তোমার নাম" in t or "your name" in t or "naam ki" in t:return "আমার নাম JARVIS।"
        if "কে বানিয়েছে" in t or "who made you" in t or "ke baniyeche" in t:return "আমাকে Biswajit বানাচ্ছে।"
        if "কেমন আছ" in t or "how are you" in t:return "আমি ঠিক আছি। তোমার সঙ্গেই আছি।"
        if re.search(r"\b(hi|hello|hey)\b",t):return "হ্যাঁ, বলো। আমি শুনছি।"
        return "হুম, শুনছি। বলো।"

class Proactive:
    def __init__(self,silence=90,cooldown=300):self.silence=silence;self.cooldown=cooldown;self.last=time.monotonic();self.last_prompt=0
    def spoke(self):self.last=time.monotonic()
    def check(self,quiet):
        now=time.monotonic()
        if quiet or now-self.last<self.silence or now-self.last_prompt<self.cooldown:return None
        self.last_prompt=now;return "কী হলো? অনেকক্ষণ কিছু বলছো না। সব ঠিক আছে তো?"

def speak(text):
    if shutil.which("termux-tts-speak"): subprocess.run(["termux-tts-speak","-l","ben-IN",text],check=False); return True
    return False

def listen():
    if not shutil.which("termux-speech-to-text"):return None
    p=subprocess.run(["termux-speech-to-text"],capture_output=True,text=True,check=False);return p.stdout.strip() or None
