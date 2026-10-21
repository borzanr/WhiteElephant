#!/usr/bin/env bash
# Copia site/ para o repositório servido pelo GitHub Pages (../whiteelephant-site)
# e cria um commit lá. Não faz push: publicar continua sendo um passo manual.
#
# A fonte fica neste repositório, ao lado do app, porque a página do convite e
# share/InviteLink.kt formam um contrato: mudar um sem o outro quebra os convites.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEST="$HERE/../../whiteelephant-site"
[ -d "$DEST/.git" ] || { echo "Repositório do site não encontrado em $DEST"; exit 1; }

find "$DEST" -mindepth 1 -maxdepth 1 ! -name .git ! -name README.md -exec rm -rf {} +
cp -r "$HERE"/. "$DEST"/
rm -f "$DEST/publish.sh"

cd "$DEST"
git add -A
if git diff --cached --quiet; then echo "Nada mudou no site."; exit 0; fi
git commit -q -m "Atualiza site a partir de WhiteElephant/site"
git log --oneline -1
echo "Pronto. Para publicar: cd $DEST && git push"
