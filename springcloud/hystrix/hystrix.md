### hystrix

* 服务降级
    - 优先保障核心服务，非核心服务不可用或弱可用
    - 通过HystrixCommand注解指定
    - fallbackMethod（回退函数）实现降级逻辑

* 依赖隔离
    - 线程隔离
    - hystrix自动实现了依赖隔离
    
* 服务熔断
* 监控（hystrix dashboard）

引入依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-client</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>

加入注解SpringCloudApplication或EnableCircuitBreaker

    @SpringCloudApplication
    public class OrderApplication {
        public static void main(String[] args) {
            SpringApplication.run(OrderApplication.class);
        }
    }
    
    其中@SpringCloudApplication包含多个注解
        @SpringBootApplication
        @EnableDiscoveryClient
        @EnableCircuitBreaker
        public @interface SpringCloudApplication {
        }

实现类

    当服务方不能提供服务时，则调用fallback方法
    @RestController
    @RequestMapping("/hystrix")
    public class HystrixController {
    
        @HystrixCommand(fallbackMethod = "fallback")
        @GetMapping("/getList")
        public String getList(){
            RestTemplate restTemplate=new RestTemplate();
            String str = restTemplate.getForObject("http://localhost:8701/product/list", String.class);
            return str;
        }
    
        private String fallback(){
            return "请稍后再试";
        }
    }

使用默认fallback，可供多个方法默认调用

    @RestController
    @RequestMapping("/hystrix")
    @DefaultProperties(defaultFallback = "defaultFallback")
    public class HystrixController {
    
        @HystrixCommand
        @GetMapping("/getList")
        public String getList(){
            RestTemplate restTemplate=new RestTemplate();
            String str = restTemplate.getForObject("http://localhost:8701/product/list", String.class);
            return str;
        }
    
        private String defaultFallback(){
            return "请稍后再试";
        }
    }
### 设置超时时间

product 模拟用时2秒
    
    @RestController
    @RequestMapping("/product")
    public class ProductController {
    
        @Autowired
        private CloudInfoRepository cloudInfoRepository;
    
        @GetMapping("/list")
        public List<CloudInfo> list() throws InterruptedException {
            Thread.sleep(2000);
            return cloudInfoRepository.findById(1);
        }
    }
    
order 调用超时设置

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    @GetMapping("/getList")
    public String getList(){
        RestTemplate restTemplate=new RestTemplate();
        String str = restTemplate.getForObject("http://localhost:8701/product/list", String.class);
        return str;
    }
    
### 服务熔断
    
实现类
 
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled",value = "true"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value = "60")
    })
    @GetMapping("/getList")
    public String getList(@RequestParam("num")Integer num){
        if (num%2==0)
            return "success";
        RestTemplate restTemplate=new RestTemplate();
        String str = restTemplate.getForObject("http://localhost:8701/product/list", String.class);
        return str;
    }
    
    private String defaultFallback(){
        return "请稍后再试";
    }
    
其中num为偶数则返回success，为奇数则调用product，product操作时间为2秒，会导致服务超时进入fallback

其中连续多次访问num为奇数的服务
http://localhost:8702/hystrix/getList?num=1

此时访问num为2的服务会出现进入fallback方法，但是一段时间后就正确返回success，为服务熔断
http://localhost:8702/hystrix/getList?num=2

Circuit Breaker断路器
circuitBreaker.enabled 设置服务熔断
circuitBreaker.requestVolumeThreshold 
circuitBreaker.sleepWindowInMilliseconds"
circuitBreaker.errorThresholdPercentage",

### 在配置文件中设置超时和熔断

    hystrix:
      command:
        default: #全局的设置
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 3000
        getList: #单独给方法设置
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 3000

HystrixCommand默认commandKey为方法名

    @HystrixCommand
    @GetMapping("/getList")
    public String getList(@RequestParam("num")Integer num)

### 引入dashboard

maven依赖

    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
            <version>2.1.2.RELEASE</version>
    </dependency>
    
启动类

    @EnableEurekaClient
    @EnableFeignClients
    @SpringCloudApplication
    @EnableHystrixDashboard
    public class OrderApplication {
        public static void main(String[] args) {
            SpringApplication.run(OrderApplication.class);
        }
    }

访问 http://localhost:8702/hystrix
    