#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "=== Personal AI local brain setup ==="

pkg update -y
pkg install -y git cmake curl

cd "$HOME"
if [ ! -d llama.cpp ]; then
  git clone --depth 1 https://github.com/ggml-org/llama.cpp.git
else
  cd llama.cpp
  git pull --ff-only
  cd "$HOME"
fi

cd "$HOME/llama.cpp"
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release -DGGML_OPENMP=OFF
cmake --build build --config Release --parallel 4 --target llama-server llama-cli

mkdir -p "$HOME/personal-ai-models"
cd "$HOME/personal-ai-models"

MODEL="qwen2.5-1.5b-instruct-q4_k_m.gguf"
URL="https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf?download=true"

if [ ! -f "$MODEL" ]; then
  echo "Downloading local brain (~1.1 GB)..."
  curl -L --fail --retry 3 -o "$MODEL" "$URL"
fi

pkill -f "llama-server.*8080" 2>/dev/null || true

echo "Starting Personal AI local brain on 127.0.0.1:8080 ..."
nohup "$HOME/llama.cpp/build/bin/llama-server"   -m "$HOME/personal-ai-models/$MODEL"   -c 4096   -ngl 0   --host 127.0.0.1   --port 8080   > "$HOME/personal-ai-llm.log" 2>&1 &

sleep 3

if curl -s --max-time 3 http://127.0.0.1:8080/health | grep -q '"status":"ok"'; then
  echo
  echo "LOCAL BRAIN IS RUNNING."
  echo "Keep Termux running in the background."
else
  echo
  echo "Server did not become ready yet."
  echo "Check: tail -n 80 ~/personal-ai-llm.log"
fi
