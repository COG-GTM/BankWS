#!/usr/bin/env sh
# Renders architecture.dot to PNG and SVG. Needs Graphviz (apt-get install graphviz fonts-dejavu).
set -e
cd "$(dirname "$0")"
dot -Tpng -Gdpi=110 architecture.dot -o architecture.png
dot -Tsvg architecture.dot -o architecture.svg
