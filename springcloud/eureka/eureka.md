## Eureka高可用

基于Netflix Eureka做了二次封装，两个组件组成：

* Eureka Server 注册中心
* Eureka Client 服务注册
* 心跳检测、健康检查、负载均衡

eureka server 和 client 采用心跳机制，server不停检查client是否上线，在一定时间统计client的上线率。

低于某个比率则警告上线率太低，不知道是否上线还是下线，则当作为上线。

![img](https://github.com/lwsh1995/java-application/blob/master/springcloud/eureka/high.png)

公共maven依赖

     <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.1.5.RELEASE</version>
            <relativePath/>
        </parent>
        <groupId>com.spring</groupId>
        <artifactId>eureka</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <name>eureka</name>
    
        <properties>
            <java.version>1.8</java.version>
            <spring-cloud.version>Greenwich.SR1</spring-cloud.version>
        </properties>
    
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>${spring-cloud.version}</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
    
        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
    
    </project>

### Eureka Server 注册中心

maven依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>

application.yml

    spring:
      application:
        name: eurekaOne
      profiles: eurekaOne
    eureka:
      instance:
        hostname: eurekaOne
      client:
        service-url:
          defaultZone: http://127.0.0.1:8762/eureka,http://127.0.0.1:8763/eureka
        register-with-eureka: false #不向自己注册自己
      server:
        enable-self-preservation: false #开发环境设置，不用于生产环境
    server:
      port: 8761
    ---
    spring:
      application:
        name: eurekaTwo
      profiles: eurekaTwo
    eureka:
      instance:
        hostname: eurekaTwo
      client:
        service-url:
          defaultZone: http://127.0.0.1:8761/eureka,http://127.0.0.1:8763/eureka
        register-with-eureka: false
      server:
        enable-self-preservation: false
    server:
      port: 8762
    ---
    spring:
      application:
        name: eurekaThr
      profiles: eurekaThr
    eureka:
      instance:
        hostname: eurekaThr
      client:
        service-url:
          defaultZone: http://127.0.0.1:8761/eureka,http://127.0.0.1:8762/eureka
        register-with-eureka: false
      server:
        enable-self-preservation: false
    server:
      port: 8763
    
通过程序参数启动不同的eureka实例  java -jar EurekaServer.jar --spring.profiles.active=eurekaOne

启动类`EurekaApplication.java`

    /* eureka既是服务端也是客户端，需要配置注册中心 */
    @SpringBootApplication
    @EnableEurekaServer
    public class EurekaApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(EurekaApplication.class, args);
        }
    
    }

### Eureka Clinet 

maven依赖

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

application.yml

    eureka:
      instance:
        hostname: client
      client:
        service-url:
          defaultZone: http://127.0.0.1:8761/eureka,http://127.0.0.1:8762/eureka,,http://127.0.0.1:8763/eureka
    spring:
      application:
        name: client
    server:
      port: 8080
      
启动类`EurekaApplication.java`

    @SpringBootApplication
    @EnableEurekaClient
    public class ClientApplication {
        public static void main(String[] args) {
            SpringApplication.run(ClientApplication.class, args);
        }
    }
