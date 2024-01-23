FROM openjdk:17

ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD
ARG PROFILE
ARG HOSTNAME
ARG EUREKA_URL
ARG GATEWAY_URL
ARG FILE_SERVER

ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV PROFILE=${PROFILE}
ENV HOSTNAME=${HOSTNAME}
ENV EUREKA_URL=${EUREKA_URL}
ENV GATEWAY_URL=${GATEWAY_URL}
ENV FILE_SERVER=${FILE_SERVER}

COPY build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]