#FROM openjdk:8-jdk-alpine
FROM gcr.io/google-appengine/openjdk
VOLUME /tmp
EXPOSE 8001
ADD target/taxamount-dataload.jar app1.jar
#ADD src/main/resources/json-key.json json-key.json
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app1.jar" ]