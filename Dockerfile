FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8001
ADD target/taxamount-dataload.jar app1.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app1.jar" ]