#!/usr/bin/env bash
set -e

# -----------------------------
# Viscan Installation Script
# -----------------------------
# Usage:
# ./install.sh [ENV_DIR] [PYTHON_VERSION] [PACKAGES] [CHANNELS] [UPDATE_SHEBANG]
#   ENV_DIR       : Environment install path (default: ./tools_env)
#   PYTHON_VERSION: Python version (default: 3.11)
#   PACKAGES      : Comma-separated packages (default: fastp,kraken2,bowtie2,recentrifuge)
#                   Optional: specify versions like kraken2=2.17.1
#   CHANNELS      : Comma-separated channels (default: conda-forge,bioconda)
#   UPDATE_SHEBANG: yes/no (default: yes)

# -----------------------------
# 1. Arguments
# -----------------------------
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MICROMAMBA="$ROOT_DIR/micromamba"

ENV_DIR="${1:-$ROOT_DIR/tools_env}"
PYTHON_VERSION="${2:-3.11}"
PACKAGES="${3:-fastp,kraken2,bowtie2,recentrifuge}"
CHANNELS="${4:-conda-forge,bioconda}"
UPDATE_SHEBANG="${5:-yes}"

echo "[Viscan] Root dir: $ROOT_DIR"
echo "[Viscan] Environment dir: $ENV_DIR"
echo "[Viscan] Python version: $PYTHON_VERSION"
echo "[Viscan] Packages: $PACKAGES"
echo "[Viscan] Channels: $CHANNELS"
echo "[Viscan] Update shebang: $UPDATE_SHEBANG"

# -----------------------------
# 2. Check micromamba
# -----------------------------
if [ ! -x "$MICROMAMBA" ]; then
  echo "[ERROR] micromamba not found or not executable"
  exit 1
fi

# -----------------------------
# 3. Check if environment exists
# -----------------------------
if [ -d "$ENV_DIR" ]; then
  echo "[Viscan] Environment already exists, skipping installation"
  exit 0
fi

# -----------------------------
# 4. Prepare channels and packages
# -----------------------------
IFS=',' read -r -a CHANNEL_ARRAY <<< "$CHANNELS"
CHANNEL_ARGS=()
for ch in "${CHANNEL_ARRAY[@]}"; do
    CHANNEL_ARGS+=("-c" "$ch")
done

IFS=',' read -r -a PKG_ARRAY <<< "$PACKAGES"

# -----------------------------
# 5. Create environment
# -----------------------------
echo "[Viscan] Creating tools_env ..."

"$MICROMAMBA" create -y \
  -p "$ENV_DIR" \
  "${CHANNEL_ARGS[@]}" \
  "python=$PYTHON_VERSION" \
  "${PKG_ARRAY[@]}"

echo "[Viscan] Installation finished successfully"

# -----------------------------
# 6. Update shebangs (optional)
# -----------------------------
if [ "$UPDATE_SHEBANG" = "yes" ]; then
    echo "[Viscan] Updating Python scripts shebangs..."
    BIN_DIR="$ENV_DIR/bin"

    for f in "$BIN_DIR"/*; do
        [ -f "$f" ] || continue
        target=$(readlink -f "$f")
        if file "$target" | grep -q 'Python script'; then
            echo "[INFO] Updating shebang in $target"
            sed -i "1s|.*|#!$BIN_DIR/python|" "$target"
            chmod +x "$target"
        fi
    done
    echo "[Viscan] Shebang update completed successfully"
fi

echo "[Viscan] All tools are ready to use!"
