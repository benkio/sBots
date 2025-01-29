FROM hseeberger/scala-sbt:8u302_1.5.5_2.13.6
COPY target/scala-2.13/main.jar /usr/app/main.jar
EXPOSE 8080
ENTRYPOINT ["java","-cp","/usr/app/main.jar", "com.benkio.main.MainWebhook"]