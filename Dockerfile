FROM eclipse-temurin:17-jre 
#FROM eclipse-temurin:17-jre-alpine use in linux based server
WORKDIR /app
COPY target/payment-1.0.0.jar app.jar
#need to update jar path in server
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]