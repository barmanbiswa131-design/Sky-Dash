import time
from assistant.brain import Brain
from assistant.proactive import Proactive
from assistant.voice import available, listen, speak


def main():
    brain = Brain()
    proactive = Proactive()
    if not available():
        print("VOICE_READY=NO")
        print("Install Termux:API and the Termux:API package, then run again.")
        return
    print("JARVIS prototype started. Say 'chup thako' to pause proactive speech.")
    speak("আমি ready। তুমি বললেই শুনব।")
    while True:
        prompt = proactive.silence_prompt(brain.quiet)
        if prompt:
            speak(prompt)
        text = listen()
        if not text:
            time.sleep(1)
            continue
        proactive.user_spoke()
        print("YOU:", text)
        answer = brain.reply(text)
        if answer:
            print("AI:", answer)
            speak(answer)

if __name__ == "__main__":
    main()
