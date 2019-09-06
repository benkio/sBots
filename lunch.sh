cd ./telegrambotinfrastructure/

sbt assembly

mv ./bin/TelegramBotInfrastructure-0.0.1.jar ../richardPHJBensonBot/lib/

cd ..

ls -l ./richardPHJBensonBot/lib/ #to see the content of the lib folder

(cd ./calandroBot/; sbt run) &
(cd ./djJacobBot/; sbt run) &
(cd ./richardPHJBensonBot/; sbt run)
