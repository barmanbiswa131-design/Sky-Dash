from pathlib import Path

SPECIAL = ["<pad>", "<bos>", "<eos>", "<unk>"]

class CharTokenizer:
    def __init__(self, text=None):
        chars = sorted(set(text or ""))
        self.itos = SPECIAL + chars
        self.stoi = {c:i for i,c in enumerate(self.itos)}

    @property
    def vocab_size(self):
        return len(self.itos)

    def encode(self, text):
        unk = self.stoi["<unk>"]
        return [self.stoi["<bos>"]] + [self.stoi.get(ch, unk) for ch in text] + [self.stoi["<eos>"]]

    def decode(self, ids):
        skip = {"<pad>", "<bos>", "<eos>"}
        return "".join(self.itos[i] for i in ids if self.itos[i] not in skip)

    def save(self, path):
        Path(path).write_text("\n".join(self.itos), encoding="utf-8")

    @classmethod
    def load(cls, path):
        t = cls()
        t.itos = Path(path).read_text(encoding="utf-8").splitlines()
        t.stoi = {c:i for i,c in enumerate(t.itos)}
        return t
