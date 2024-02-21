FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY target/*.jar app.jar
COPY gcf-creds.json /gcf-creds.json
ENTRYPOINT ["java","-jar","/app.jar"]