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
    
自定义pre filter（权限校验）

    @Component
    public class TokenFilter extends ZuulFilter {
        //设置filter类型
        @Override
        public String filterType() {
            return PRE_TYPE;
        }
    
        //设置该过滤的顺序，值越小则越早执行
        @Override
        public int filterOrder() {
            return PRE_DECORATION_FILTER_ORDER-1;
        }
    
        //开启过滤器
        @Override
        public boolean shouldFilter() {
            return true;
        }
    
        //token校验
        @Override
        public Object run() throws ZuulException {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletRequest request = context.getRequest();
    
            String token = request.getParameter("token");
            if (StringUtils.isEmpty(token)){
                context.setSendZuulResponse(false);
                context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            }
            return null;
        }
    }

访问 http://localhost:8704/test/order/env?token=123

自定义post filter返回参数

    @Component
    public class ResponseHeaderFilter extends ZuulFilter {
        //设置post filter
        @Override
        public String filterType() {
            return POST_TYPE;
        }
    
        //设置post的执行顺序
        @Override
        public int filterOrder() {
            return SEND_RESPONSE_FILTER_ORDER-1;
        }
    
        //开启post过滤器
        @Override
        public boolean shouldFilter() {
            return true;
        }
    
        //返回自定义参数
        @Override
        public Object run() throws ZuulException {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletResponse response = context.getResponse();
            response.setHeader("X-T", UUID.randomUUID().toString());
            return null;
        }
    }

### zuul 限流

请求被转发之前调用（实现方式令牌桶，一定速率生成令牌，获得令牌才继续执行）

    @Component
    public class RateLimitFilter extends ZuulFilter {
        private static final RateLimiter RATE_LIMITER= RateLimiter.create(100);
    
        @Override
        public String filterType() {
            return PRE_TYPE;
        }
    
        //设置最高优先级
        @Override
        public int filterOrder() {
            return SERVLET_DETECTION_FILTER_ORDER -1 ;
        }
    
        @Override
        public boolean shouldFilter() {
            return true;
        }
    
        //获取令牌
        @Override
        public Object run() throws ZuulException {
            if (!RATE_LIMITER.tryAcquire()){
                throw new RuntimeException();
            }
            return null;
        }
    }
    
### zuul 跨域

* 在被调用的类和方法上添加@CrossOrigin注解

   
    @GetMapping("list")
    @CrossOrigin(allowCredentials = "true")
    public String list(){
        return "list";
    }
        
        
* 在zuul中添加Filter过滤器


    // C-Cross O-Origin R-Resource S-Sharing
    @Configuration
    public class CorsConfig {
    
        @Bean
        public CorsFilter corsFilter(){
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
    
            //允许跨域
            config.setAllowCredentials(true);
            //设置源，如 www.a.com
            config.setAllowedOrigins(Arrays.asList("*"));
            //设置请求头，如 order/create
            config.setAllowedHeaders(Arrays.asList("*"));
            //设置请求方法，如 GET、POST
            config.setAllowedMethods(Arrays.asList("*"));
            //设置时间
            config.setMaxAge(300l);
            //对所有路径生效
            source.registerCorsConfiguration("/**",config);
            return new CorsFilter(source);
        }

### zull超时

zuul在第一次启动时会超时，因为依赖的类会懒加载导致第一次请求会超时，由于依赖hystrix，可以yml中配置

    hystrix:
      command:
        default: #全局的设置
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 3000
