FROM adoptopenjdk/openjdk11
VOLUME /tmp
COPY build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java","-Dspring.data.mongodb.uri=mongodb://mongodb:27017/test","-jar","/app.jar"]