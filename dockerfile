FROM openjdk:21-slim
COPY target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar" , "/app/app.jar"]