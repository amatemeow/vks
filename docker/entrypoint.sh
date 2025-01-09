#!/usr/bin/env bash
exec java -jar /app/spring-boot-application.jar \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=0.0.0.0:7000 \
    -XX:+UseCGroupMemoryLimitForHeap ${MEMORY_LIMITS} \
    -Djava.security.egd=file:/dev/urandom \
    --spring.config.location=classpath://application.yml,file:///app/application.yml
