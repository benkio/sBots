# Script for sarting the bots ##################################################
#
# This script will always step into the single projects and run a test + run
#
# -r flag the script will package the common library used into the bots
################################################################################

echo "################################################################################"
echo "#                     Welcome to the bot starting script                       #"
echo "#                                                                              #"
echo "################################################################################"
RECOMPILE=false

while getopts dr option
do
    case "${option}"
    in
        r) RECOMPILE=true
           echo "selected the Recompile option";;
        ?) echo "no options selected"
    esac
done

if [ "$RECOMPILE" = true ] ;
then
    echo "Recompiling the infrastructure"
    cd ./telegrambotinfrastructure/

    sbt assembly

    echo "Moving the library to the bots lib folders"
    cp ./bin/TelegramBotInfrastructure-0.0.1.jar ../richardPHJBensonBot/lib/
    mv ./bin/TelegramBotInfrastructure-0.0.1.jar ../calandroBot/lib/
    ls -l ../calandroBot/lib/ #to see the content of the lib folder
    ls -l ../richardPHJBensonBot/lib/ #to see the content of the lib folder

    cd ..
    echo "Finish the recompilation of the infrastructure"
fi

echo "test & run"

(cd ./calandroBot/; sbt test) &
(cd ./richardPHJBensonBot/; sbt test) &
(cd ./calandroBot/; sbt run) &
# (cd ./djJacobBot/; sbt run) &
(cd ./richardPHJBensonBot/; sbt run)
