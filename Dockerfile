FROM openjdk:latest
ADD target/Excel-Oku-0.0.1-SNAPSHOT.jar Excel-Oku-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Excel-Oku-0.0.1-SNAPSHOT.jar"]