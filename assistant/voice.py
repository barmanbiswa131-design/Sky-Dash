"""Android voice bridge using Termux:API commands."""
import shutil
import subprocess


def available():
    return shutil.which("termux-speech-to-text") and shutil.which("termux-tts-speak")


def speak(text):
    if not text or not shutil.which("termux-tts-speak"):
        return False
    subprocess.run(["termux-tts-speak", "-l", "ben-IN", text], check=False)
    return True


def listen():
    if not shutil.which("termux-speech-to-text"):
        return None
    p = subprocess.run(["termux-speech-to-text"], capture_output=True, text=True, check=False)
    return p.stdout.strip() or None
