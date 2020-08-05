FROM openjdk:8-jdk-alpine
#FROM tomcat
#FROM openjdk:8-jdk
#FROM openjdk:11
VOLUME /tmp
EXPOSE 8001
ADD target/*.jar app1.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app1.jar" ]