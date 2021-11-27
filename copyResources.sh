#!/usr/bin/env bash

DEFAULT_DESTINATION_FOLDER=~/temp/mytelegrambot/
DESTINATION_FOLDER=${1:-$DEFAULT_DESTINATION_FOLDER}
echo $DESTINATION_FOLDER
find -type d -iname 'resources' -exec cp -r -n --parents {} $DESTINATION_FOLDER \;
