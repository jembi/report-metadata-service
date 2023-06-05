FROM azul/zulu-openjdk:17.0.3

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get install -y apt-utils && apt-get update && apt-get install -y lnav iputils-ping

ADD target/Metadata-1.0-SNAPSHOT-spring-boot.jar Metadata-1.0-SNAPSHOT-spring-boot.jar

ENTRYPOINT ["java", "-server", "-Xms16M", "-Xmx256M", "-jar", "Metadata-1.0-SNAPSHOT-spring-boot.jar"]