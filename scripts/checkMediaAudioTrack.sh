#!/usr/bin/env bash

###############################################################################
#   Simple bash script that checks if the mp4 files have not an audio track   #
#                                                                             #
#   It uses mediainfo, jq and fd, use it at the root of the file directory    #
###############################################################################

for file in $(fd -e mp4); do
    if ! mediainfo --Output=JSON "$file" | jq 'any(.media.track[]; .["@type"] == "Audio")' | grep -q 'true'; then
        echo "$file"
    fi
done
