FROM openjdk:8
WORKDIR /app
COPY src /app/src
RUN cd src
RUN ls
RUN cd src;cd c0; javac -encoding utf8 Main.java
