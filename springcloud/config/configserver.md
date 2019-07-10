### config server统一配置中心

git仓库：https://github.com/lwsh1995/config-server.git

创建order.yml(order工程的application.yml)
    
    spring:
      application:
        name: order
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka
    server:
      port: 8702
    PRODUCT:
      ribbon:
        NFLoadBalancerRuleClientName: com.netflix.loadbalancer.RandomRule

maven依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>

application.yml，basedir配置拉取的配置文件存放目录

    spring:
      application:
        name: config
      cloud:
        config:
          server:
            git:
              uri: https://github.com/lwsh1995/config-server
              basedir: E:\idea\java\cloud\basedir
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka
    server:
      port: 8703
      
启动类

    @SpringBootApplication
    @EnableEurekaClient
    @EnableConfigServer
    public class ConfigApplication {
        public static void main(String[] args) {
            SpringApplication.run(ConfigApplication.class);
        }
    }
    
通过http://localhost:8703/order-a.yml可以访问order.yml配置

    PRODUCT:
      ribbon:
        NFLoadBalancerRuleClientName: com.netflix.loadbalancer.RandomRule
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka
    server:
      port: 8702
    spring:
      application:
        name: order

其中order-a.yml必须给定一个后缀才能访问
通过http://localhost:8703/order-a.json | properties改变yml格式

    PRODUCT.ribbon.NFLoadBalancerRuleClientName: com.netflix.loadbalancer.RandomRule
    eureka.client.service-url.defaultZone: http://localhost:8761/eureka
    server.port: 8702
    spring.application.name: order
    
在config中获取配置的格式

/{name}-{profiles}.yml
/{lable}/{name}-{profiles}.yml

* name 指定服务名
* profiles 指定环境
* labl 指定分支
    
创建dev环境order-dev.yml，通过http://localhost:8703/order-dev.yml访问

    spring:
      application:
        name: order
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka
    server:
      port: 8702
    PRODUCT:
      ribbon:
        NFLoadBalancerRuleClientName: com.netflix.loadbalancer.RandomRule
    env: dev

创建release分支，order-test.yml。通过http://localhost:8703/release/order-dev.yml访问
    
    spring:
      application:
        name: order
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka
    server:
      port: 8702
    PRODUCT:
      ribbon:
        NFLoadBalancerRuleClientName: com.netflix.loadbalancer.RandomRule
    env: test
    label: release
    
在客户端拉取config server配置文件时，会拉取order.yml以及对应的order-{profile}.yml，

并且会把两个文件合并，有时会导致问题。一般以order.yml放置所有配置文件的公共参数。
    
在order工程中不使用application.yml的配置方式，创建bootstrap.yml文件，

springboot则会优先拉取配置文件在启动，避免读取不到配置文件报错，在启动类加@EnableDiscoveryClient

    #bootstrap.yml
    #以下采用config server模式拉取配置
    #以下采用config server模式拉取配置
    spring:
      application:
        name: order
      cloud:
        config:
          discovery:
            enabled: true
            service-id: CONFIG
          profile: dev
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8762/eureka/
         
### config server高可用

启动多个config server 服务即可