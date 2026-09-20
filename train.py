from pathlib import Path
import torch
from torch.nn import functional as F
from biswa_ai.tokenizer import CharTokenizer
from biswa_ai.model import BiswaTransformer

TEXT = Path("data/train.txt").read_text(encoding="utf-8")
tok = CharTokenizer(TEXT)
tok.save("tokenizer.txt")
ids = torch.tensor(tok.encode(TEXT), dtype=torch.long)

model = BiswaTransformer(tok.vocab_size)
opt = torch.optim.AdamW(model.parameters(), lr=3e-4)

for epoch in range(1, 51):
    x, y = ids[:-1].unsqueeze(0), ids[1:].unsqueeze(0)
    logits = model(x)
    loss = F.cross_entropy(logits.reshape(-1, tok.vocab_size), y.reshape(-1))
    opt.zero_grad()
    loss.backward()
    torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
    opt.step()
    if epoch % 5 == 0:
        print(f"epoch={epoch} loss={loss.item():.4f}")

torch.save(model.state_dict(), "biswa_ai.pt")
print("Saved biswa_ai.pt")
