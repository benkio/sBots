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
while getopts dtrs option
do
    case "${option}"
    in
        t) TEST=true
           echo "selected the Test option";;
        s) SIMULATION=true
           echo "selected the Simulation option";;
        ?) echo "no options selected"
    esac
done

sbt --supershell=false scalafmtAll

if [[ "$TEST" == true || "$SIMULATION" == true ]] ;
then
    echo "-------------------------Running Tests-------------------------"
    sbt --supershell=false test
fi

if [ "$SIMULATION" = false ] ;
then
    echo "-------------------------assembly-------------------------"

    sbt --supershell=false main/assembly

    (cd ./main/target/scala-2.13/; java -jar main.jar)
fi
