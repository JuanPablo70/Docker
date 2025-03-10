# Docker

This learning project, inspired by the Udemy course [Master Spring Boot 3 & Spring Framework 6 with Java](https://www.udemy.com/course/spring-boot-and-spring-framework-tutorial-for-beginners/), was developed using Docker and Docker Hub.

### Prerequisites

To use this project, you need to have knowledge of:

+ Java - Object Oriented Programming Language.
+ Spring Boot - Create stand-alone Spring applications.
+ Docker - Accelerated Container Application Development.
+ Postman - API testing platform.

### Installing the Project

To download this project, run the following command down below.

```
git clone https://github.com/JuanPablo70/Docker.git
```

### About this Project

+ **How does Traditional Deployment work?**
  + Typically, the deployment process is described in a document.
  + Let's say the operations team needs to deploy a new environment and to do that, they follow the steps to:
    + Setup Hardware
    + Setup OS (Linux, Windows, Mac, ...)
    + Install the right Software (Java, Python, NodeJs, ...)
    + Setup Application Dependencies
    + Install Application
  + This is a very, very manual approach. This takes a lot of time, and there is a very, very high chance that someone makes mistakes.

+ **Understanding Deployment process with Docker**
  + Docker enables a very simple deployment process:
    + OS doesn't matter
    + Programming Language doesn't matter
    + Hardware doesn't matter
  + Developer team creates a Docker Image.
  + Operations team run the Docker Image with a simple command:
    ```
    docker container run -d -p [host-port]:[container-port] [dockerhub-repository]/[project]:[tag]
    ```
    
  *Once a Docker image is created, irrespective of what the Docker image contains, it is ran the same way*
    
  + To list the containers the command is:
    ```
    docker container ls 
    ```
  + To stop an specific container the command is:
    ```
    docker container stop [CONTAINER ID] 
    ```
  + Docker image has everything is needed to run an application:
    + OS
    + Application Runtime (JDK, Python, NodeJs, ...)
    + Application code and dependencies

+ **Understanding how Docker works?**
  + Standaridized Application Packaging: It provides a way to create a Docker image which contains the same packaging for al types of applications either it is a Java, Python or NodeJs application. The format of the final Docker image and the way you would run it are exactly the same.
  + Multi-platform Support: It can be ran on a local machine, in data centers or in the cloud (AWS, Azure, GCP).
  + Isolation: Even there're multiple containers running on the same machine, each container is isolated from the other containers.

+ **What is happening in the background?**
  + Docker image is downloaded from Docker Registry (Docker Hub)
      ```
      docker container run -d -p [host-port]:[container-port] [dockerhub-repository]/[project]:[tag]
      ```
    + *Image* is a set of bytes. These bytes are hosted inside the Docker regostry, inside the Docker Hub.
    + *Container* is a version of the image running (Running Image).
    + *[dockerhub-repository]/[project]* is the repository name in Docker Hub.
    + *[tag]* is tied to a specific release or a version of a specific application or a microservice.
    + *-p [host-port]:[container-port]* maps internal docker port (container-port) to a port on the host (host-port).
      + By default, Docker uses its own internal network caled bridge network.
      + The host port is mapped so that users can access the application.
    + *-d* is the detached mode (don't tie up the terminal).

+ **Docker Terminology**
  + **Docker Image:** A package representing specific version of tour application (or software). It contains everything the app needs (OS, software, code, dependencies).
  + **Docker Registry:** A place to store Docker images.
  *Docker Hub is a registry to host Docker images*
  + **Doker Repository:** Docker images for a specific app (tags are used to differentiate different images).
  + **Docker Container:** Runtime instance of a Docker image.
  + **Dockerfile:** File with instructions to create a Docker image.

+ **Dockerfile**
  Dockerfile contains instructions to create Docker images.

  + Dockerfile 1 - Basic Docker image
    ```
    FROM openjdk:21-jdk-slim
    COPY target/*.jar app.jar
    EXPOSE 5000
    ENTRYPOINT ["java","-jar","/app.jar"]
    ```
  
    + **FROM** - sets a base image. Docker uses the OpenJDK 21 JDK image (in its slim form) as the base image for building the Docker container. This image will contain everything needed to run and develop Java applications using OpenJDK 21
    + **COPY** - copies new files or directories into image
    + **EXPOSE** - informs Docker about the port that the container listens on at runtime
    + **ENTRYPOINT** - configures a command that will be run at container launch
   
  + Dockerfile 2 - Build Jar File - Multi Stage
    ```
    FROM maven:3.9.6-amazoncorretto-21-al2023 AS build
    WORKDIR /home/app
    COPY . /home/app
    RUN mvn -f /home/app/pom.xml clean package
    
    FROM openjdk:21-jdk-slim
    EXPOSE 5000
    COPY --from=build /home/app/target/*.jar app.jar
    ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]
    ```

    In the previous Dockerfile, the jar file creation had to be done separately to copy it in the app.jar file. The best practice is to build everything that is needed inside the Docker image.

    To ensure the entire build process is in the Docker image, now in the Dockerfile there're two stages.

    + The first stage is to build the jar file
      + **FROM** - sets the base image maven:3.9.6-amazoncorretto-21-al2023 to build the jar file and this stage is named as build
      + **WORKDIR** - sets the working directory
      + **COPY** - copies everithing that is inside the project folder to the working directory
      + **RUN** - runs mvn clean package

    + The second stage is to run the jar file
      + It's the same base image
      + It's on the same port
      + Instead of copying the jar file from the local machine, it is copying it from the build stage to the app.jar file
      + It as the same command to run the jar file

    *It is recommended to use two different images because the Maven image might contain a lot of other things than just the open JDK. And when the container is running, it's better to have a small container image ad possible*
      
  + Dockerfile 3 - Improve Layer Caching
    ```
    FROM maven:3.9.6-amazoncorretto-21-al2023 AS build
    WORKDIR /home/app
    
    COPY ./pom.xml /home/app/pom.xml
    COPY ./src/main/java/com/spring/learning_docker/LearningDockerApplication.java	/home/app/src/main/java/com/spring/learning_docker/LearningDockerApplication.java
    
    RUN mvn -f /home/app/pom.xml clean package
    
    COPY . /home/app
    RUN mvn -f /home/app/pom.xml clean package
    
    FROM openjdk:21-jdk-slim
    EXPOSE 5000
    COPY --from=build /home/app/target/*.jar app.jar
    ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]
    ```

    In the previous Dockerfile if there's a small code change, it will cause the entire application to be rebuilt again and it takes a long time.

    Docker uses something called layering. Each command that is executed in the image can create a separate layer and Docker tries to reuse layers as much as possible even when nothing has changed with previous layers in the next build.

    The great thing about this approach of adding layers 3, 4, and 5 in the Dockerfile is that it reduces the time to build the Docker image. Since the pom.xml and the SpringBootApplication class don’t change frequently, the first five layers will be reused by Docker. This means that dependencies will only be downloaded the first time the Docker image is built. In subsequent builds, Docker won't download the dependencies again unless there is a change in either the pom.xml or SpringBootApplication class.
    
  To build the Docker image, you have to be in the folder where the Dockerfile is and execute the following command:
  
  ```
  docker build -t [dockerhub-repository]/[project-name]:[tag] .
  ```

  *Do not forget to replace the repository, project name and tag*

  To see the list of Docker images, execute the following command:
  ```
  docker image list
  ```

### Build With

+ [Spring Initializr](https://start.spring.io) - Tool used to set up Spring Boot projects.
+ [Maven](https://maven.apache.org) - Software project management and comprehension tool.
+ [Docker Hub](https://hub.docker.com) - Effortlessly store, manage, and deploy containerized apps.

### Version

1.0

### Author

[Juan Pablo Sánchez Bermúdez](https://github.com/JuanPablo70)
