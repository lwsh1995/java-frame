## Feign

* 声明式REST客户端
* 采用基于接口的注解

maven 依赖
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-feign</artifactId>
        <version>1.4.6.RELEASE</version>
    </dependency>
    
启动类

    @SpringBootApplication
    @EnableEurekaClient
    @EnableFeignClients
    public class OrderApplication {
        public static void main(String[] args) {
            SpringApplication.run(OrderApplication.class);
        }
    }
    
接口类

    @FeignClient("product")
    public interface ProductClient {
        @GetMapping("/product/list")
        List<CloudInfo> productList();
    }
    
调用

    @RestController
    @RequestMapping("/order")
    public class OrderController {
        @Autowired
        ProductClient productClient;
    
        @GetMapping("/getProduct")
        public String getProduct(){
            List<CloudInfo> list = productClient.productList();
            return list.toString();
        }
    }
