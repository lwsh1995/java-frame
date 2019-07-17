### 构建docker eureka应用

在eureka应用目录中创建Dockerfile文件

    eureka
        -src
        -target
        -Dockerfile
        -pom.xml

编写Dockerfile（引入网易云java8镜像）

    FROM hub.c.163.com/library/java:8-alpine
    ADD target/*.jar app.jar
    EXPOSE 8761
    ENTRYPOINT ["java","-jar","/app.jar"]

在terminal中重构项目并构建docker镜像启动

    E:\idea\java\cloud\eureka>mvn clean package -Dmaven.test.spik=true
    E:\idea\java\cloud\eureka>docker build -t cloud/eureka .
    E:\idea\java\cloud\eureka>docker run -d -p 8762:8761 cloud/eureka

访问http://localhost:8762/