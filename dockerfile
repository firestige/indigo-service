FROM adoptopenjdk:11-jre-hotspot
RUN mkdir /opt/app
COPY indigo-service-0.1-beta.jar /opt/app
RUN chmod 755 /opt/app/indigo-service-0.1-beta.jar
CMD ["java", "-jar", "/opt/app/indigo-service-0.1-beta.jar"]