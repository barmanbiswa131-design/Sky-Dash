import torch
from biswa_ai.tokenizer import CharTokenizer
from biswa_ai.model import BiswaTransformer

tok = CharTokenizer.load("tokenizer.txt")
model = BiswaTransformer(tok.vocab_size)
model.load_state_dict(torch.load("biswa_ai.pt", map_location="cpu"))
model.eval()

def generate(prompt, max_new=180, temperature=0.8):
    ids = tok.encode("<bos> user: " + prompt + "\nassistant:")
    x = torch.tensor(ids, dtype=torch.long).unsqueeze(0)
    for _ in range(max_new):
        with torch.no_grad():
            logits = model(x[:, -model.max_len:])[:, -1, :] / temperature
            probs = torch.softmax(logits, dim=-1)
            nxt = torch.multinomial(probs, 1)
        x = torch.cat([x, nxt], dim=1)
        if nxt.item() == tok.stoi["<eos>"]:
            break
    return tok.decode(x[0].tolist()).split("assistant:", 1)[-1].strip()

print("Biswa AI — local chat. Type /exit to quit.")
while True:
    q = input("You: ")
    if q.strip() == "/exit":
        break
    print("AI:", generate(q))
