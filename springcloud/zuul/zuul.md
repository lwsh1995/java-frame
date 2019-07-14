### zuul

特点：路由和过滤器

过滤器4中API
* 前置（Pre）：限流；鉴权；参数校验；请求转发
* 后置（Post）：统计；日志；
* 路由（Route）
* 错误（Error）

maven 依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
    </dependency>
    
bootstrap.yml

    spring:
      application:
        name: gateway
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka/
    server:
      port: 8704

 
启动类

    @SpringBootApplication
    @EnableZuulProxy
    public class GatewayApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(GatewayApplication.class, args);
        }
    
    }

zuul默认访问方式
访问order 服务 http://localhost:8702/order/env
可通过zuul敢问 http://localhost:8704/order/order/env
url中第一个order为服务名

zuul自定义路由yml配置

    zuul:
      routes:
        myOrder:
          path: /myOrder/**
          serviceId: order #配置order服务的路由
    简洁写法：serviceId：路由路径
    zuul:
      routes:
        order: /myOrder/**

访问路径：http://localhost:8704/myOrder/order/env

zuul禁用地址路由访问

    zuul:
      routes:
        order: /myOrder/**
      ignored-patterns: # set(String)，使用- 配置多个参数
        - /**/order/env
        - /**/order/getProduct

zuul配置cookie传递（默认不传递cookie到其他服务）

    zuul:
      routes:
        myOrder:
          path: /myOrder/**
          serviceId: order
          sensitiveHeaders: #该字段设置为空

zuul查看路由规则

    management:
      endpoints:
        web:
          exposure:
            include: routes
            
访问 http://localhost:8704/actuator/routes

zuul采用动态配置（可动态修改路由）

    git创建gateway-dev.yml
    zuul:
      routes:
        order: /test/**
    management:
      endpoints:
        web:
          exposure:
            include: routes
            
    maven加入
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-bus-amqp</artifactId>
           <version>2.1.2.RELEASE</version>
       </dependency>
       
    yml配置
    spring:
      application:
        name: gateway
      cloud:
        config:
          discovery:
            enabled: true
            service-id: CONFIG
          profile: dev
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka/
    server:
      port: 8704
      
    动态刷新
    @Component
    public class ZuulConfig {
        @ConfigurationProperties("zuul")
        @RefreshScope
        public ZuulProperties zuulProperties(){
            return new ZuulProperties();
        }
    }

zuul高可用

    zuul多个节点注册到eureka上
    ningx和zuul使用
    
    