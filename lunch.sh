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
RECOMPILE=false  # Recompile the infrastructure and put it to the dependent project as an external lib
TEST=false       # Run sbt test on the dependent libs and infrastructure
SIMULATION=false # RECOMPILE + TEST, but not sbt run
while getopts dtrs option
do
    case "${option}"
    in
        r) RECOMPILE=true
           echo "selected the Recompile option";;
        t) TEST=true
           echo "selected the Test option";;
        s) SIMULATION=true
           echo "selected the Simulation option";;
        ?) echo "no options selected"
    esac
done

(cd ./calandroBot/; sbt --supershell=false scalafmt && sbt --supershell=false test:scalafmt) &
(cd ./aBarberoBot/; sbt --supershell=false scalafmt && sbt --supershell=false test:scalafmt) &
(cd ./richardPHJBensonBot/; sbt --supershell=false scalafmt && sbt --supershell=false test:scalafmt) &
    (cd ./telegrambotinfrastructure/; sbt --supershell=false scalafmt && sbt --supershell=false test:scalafmt)

if [[ "$RECOMPILE" == true || "$SIMULATION" == true ]] ;
then
    echo "Recompiling the infrastructure"
    cd ./telegrambotinfrastructure/

    sbt --supershell=false assembly

    echo "Moving the library to the bots lib folders"
    cp ./bin/TelegramBotInfrastructure-0.0.1.jar ../richardPHJBensonBot/lib/
    cp ./bin/TelegramBotInfrastructure-0.0.1.jar ../aBarberoBot/lib/
    mv ./bin/TelegramBotInfrastructure-0.0.1.jar ../calandroBot/lib/
    ls -l ../calandroBot/lib/ #to see the content of the lib folder
    ls -l ../aBarberoBot/lib/ #to see the content of the lib folder
    ls -l ../richardPHJBensonBot/lib/ #to see the content of the lib folder

    cd ..
    echo "Finish the recompilation of the infrastructure"
fi

if [[ "$TEST" == true || "$SIMULATION" == true ]] ;
then
    echo "Running Tests"
    (cd ./calandroBot/; sbt --supershell=false test) &
    (cd ./aBarberoBot/; sbt --supershell=false test) &
    (cd ./richardPHJBensonBot/; sbt --supershell=false test) &
    (cd ./telegrambotinfrastructure/; sbt --supershell=false test)
fi

if [ "$SIMULATION" = false ] ;
then
    echo "run"

    (cd ./calandroBot/; sbt --supershell=false run) &
    (cd ./aBarberoBot/; sbt --supershell=false run) &
    (cd ./richardPHJBensonBot/; sbt --supershell=false run)

fi
