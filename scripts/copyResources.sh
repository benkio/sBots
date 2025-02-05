#!/usr/bin/env bash

###############################################################################
#         copy the content of `resources` folder into another location        #
###############################################################################

DEFAULT_DESTINATION_FOLDER=~/temp/sBots/
DESTINATION_FOLDER=${1:-$DEFAULT_DESTINATION_FOLDER}
echo $DESTINATION_FOLDER
find -type d -iname 'resources' -exec cp -r -n --parents {} $DESTINATION_FOLDER \;
