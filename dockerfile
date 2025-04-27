FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/trophy-bot-1.0-SNAPSHOT.jar /app/discord-bot.jar

CMD ["java", "-jar", "discord-bot.jar"]
