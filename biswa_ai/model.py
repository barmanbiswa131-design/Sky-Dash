import torch
from torch import nn

class BiswaTransformer(nn.Module):
    def __init__(self, vocab_size, d_model=192, nhead=4, layers=4, ff=512, max_len=512):
        super().__init__()
        self.token = nn.Embedding(vocab_size, d_model)
        self.pos = nn.Embedding(max_len, d_model)
        block = nn.TransformerEncoderLayer(d_model=d_model, nhead=nhead, dim_feedforward=ff, dropout=0.1, batch_first=True, activation="gelu")
        self.blocks = nn.TransformerEncoder(block, num_layers=layers)
        self.norm = nn.LayerNorm(d_model)
        self.head = nn.Linear(d_model, vocab_size, bias=False)
        self.max_len = max_len

    def forward(self, x):
        _, t = x.shape
        pos = torch.arange(t, device=x.device).unsqueeze(0)
        h = self.token(x) + self.pos(pos)
        mask = torch.triu(torch.ones(t, t, device=x.device), diagonal=1).bool()
        h = self.blocks(h, mask=mask)
        return self.head(self.norm(h))
