#!/usr/bin/env bash

# Run from the root of the project: ./scripts/copyTokensFromDropbox.sh

for bot in calandroBot aBarberoBot m0sconiBot richardPHJBensonBot xahLeeBot youTuboAncheI0Bot; do
    mkdir -p "modules/bots/$bot/src/main/resources"
    cp ~/Dropbox/sBots/"$bot"/src/main/resources/*.token "modules/bots/$bot/src/main/resources/" 2>/dev/null || true
done
