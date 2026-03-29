FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY jdbc-webapp ./jdbc-webapp

WORKDIR /app/jdbc-webapp
RUN mvn clean package

FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/jdbc-webapp/target/movie-review-jdbc.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
