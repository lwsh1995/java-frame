### config 自动刷新

config server 提供/bus-refresh接口由git调用刷新，在config server和client端通过

消息组件进行配置动态刷新。

在config server 中引入

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>

启动config server 则会使用默认配置在rabbitmq创建一个队列

    springCloudBus.anonymous.HaC1VSWuR6absFzmsQCz1A
    
在config server的application.yml中暴露所有路径

    management:
      endpoints:
        web:
          exposure:
            include: "*"
    
访问config的 post http://localhost:8703/actuator/bus-refresh 刷新配置
    
在order引入bus

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>

启动order则会在rabbitmq创建一个队列

    springCloudBus.anonymous.S2sHKndQRdOND6NmlAOqVg
    
order中创建访问env属性的controller

    @RestController
    @RequestMapping("/order")
    @RefreshScope
    public class OrderController {
    
        @Value("${env}")
        private String env;
    
        @GetMapping("/env")
        public String env(){
            return env;
        }
    }

启动order，在git仓库中修改order-dev.yml文件的env值，并调用post http://localhost:8703/actuator/bus-refresh 刷新

此时访问http://localhost:8702/order/env可以动态更改env的值不需要重启order应用


### order中使用前缀方式配置

在配置文件中引入entity前缀

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
    env:
      test2
    label:
      release
    
    entity:
      name: config
      version: one

配置组件就可以引入使用

    @Data
    @Component
    @ConfigurationProperties("entity")
    @RefreshScope
    public class EntityConfig {
        private String name;
        private String version;
    }

使用

    @RestController
    @RequestMapping("/config")
    public class EntityConfigController {
        @Autowired
        private EntityConfig entityConfig;
        @GetMapping("/show")
        public String show(){
            return entityConfig.toString();
        }
    }

### 使用webhooks

配置git的webhooks ：post http://localhost:8703/actuator/bus-refresh

则每次push修改的配置文件后则自动刷新，不需要手动调用http://localhost:8703/actuator/bus-refresh

如使用natapp的免费隧道进行外网映射到本地的http://localhost:8703/actuator/bus-refresh