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
TEST=false       # Run sbt test on the dependent libs and infrastructure
SIMULATION=false # TEST, but not sbt run
WEBHOOK=false    # Runs the webhook instead of the polling one
while getopts dtsw option
do
    case "${option}"
    in
        t) TEST=true
           echo "selected the Test option";;
        s) SIMULATION=true
           echo "selected the Simulation option";;
        w) WEBHOOK=true
           echo "selected the webhook opton";;
        ?) echo "no options selected"
    esac
done

sbt --supershell=false fix

if [[ "$TEST" == true || "$SIMULATION" == true ]] ;
then
    echo "-------------------------Running Tests-------------------------"
    sbt --supershell=false test
fi

if [ "$SIMULATION" = false ] ;
then
    echo "-------------------------Assembly-------------------------"

    sbt --supershell=false main/assembly

    if [ "$WEBHOOK" = false ] ;
    then
        echo "-------------------------Run Polling Bots-------------------------"
        cp ./botDB.sqlite3 ./modules/main/target/
        (cd ./modules/main/target/scala-3.7.0/; java -cp main.jar com.benkio.main.MainPolling)
    else
        echo "-------------------------Run Webhook Bots-------------------------"
        cp ./botDB.sqlite3 ./modules/main/target/
        (cd ./modules/main/target/scala-3.7.0/; java -cp main.jar com.benkio.main.MainWebhook)
    fi


fi
