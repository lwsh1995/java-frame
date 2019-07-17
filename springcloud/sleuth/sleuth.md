### 链路监控sleuth

order、product 引入maven依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>

调整日志级别

    logging:
      level:
        root: INFO
        org.springframework.web.servlet.DispatcherServlet: DEBUG
        org.springframework.cloud.sleuth: DEBUG


访问http://localhost:8702/order/getProduct日志输出
order：2019-07-16 22:17:29.198  INFO [product,7db8cd28aac85634,2c9c50e43ef66f09,false] 5488 --- [nio-8701-exec-1] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory
product：2019-07-16 22:20:09.453  INFO [order,bce8262d0b6d7275,bce8262d0b6d7275,false] 9000 --- [nio-8702-exec-1] c.n.l.DynamicServerListLoadBalancer      : DynamicServerListLoadBalancer for client product initialized: DynamicServerListLoadBalancer:{NFLoadBalancer:name=product,current list of Servers=[localhost:8701],Load balancer stats=Zone stats: {defaultzone=[Zone:defaultzone;	Instance count:1;	Active connections count: 0;	Circuit breaker tripped count: 0;	Active connections per server: 0.0;]

### zipkin

安装zipkin

docker run -d -p 9411:9411 openzipkin/zipkin

访问：http://localhost:9411/zipkin/

引入依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth-zipkin</artifactId>
    </dependency>

其中可用以下依赖代替上面依赖，因为包含了以上两个依赖（包含sleuth和zipkin）

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-zipkin</artifactId>
        <version>2.1.1.RELEASE</version>
    </dependency>

yml配置

    spring
      zipkin:
        base-url: http://localhost:9411/ #抽取数据到zipkin
      sleuth:
        sampler:
          probability: 0.5 #抽取50%的数据