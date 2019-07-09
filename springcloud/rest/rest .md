## RestTemplate调用

### product提供服务

    @RestController
    @RequestMapping("/product")
    public class ProductController {
    
        @Autowired
        private CloudInfoRepository cloudInfoRepository;
    
        @GetMapping("/list")
        public List<CloudInfo> list(){
            return cloudInfoRepository.findById(1);
        }
    }

### 第一种调用方式

该方式指定ip和端口，不能灵活应对服务方ip和端口改变的情况。

当服务方提供多个服务时，不能进行负载均衡。

    @RestController
    @RequestMapping("/order")
    public class OrderController {
    
        @GetMapping("/getProduct")
        public String getProduct(){
            RestTemplate restTemplate=new RestTemplate();
            List list = restTemplate.getForObject("http://localhost:8701/product/list", List.class);
        return list.toString();
        }
    }

### 第二种方式
该方式通过LoadBalancerClient方式获取serviceId，可以进行负载均衡，但重复代码较多
    
        @Autowired
        LoadBalancerClient loadBalancerClient;
    
        @GetMapping("/getProduct")
        public String getProduct(){
            RestTemplate restTemplate=new RestTemplate();
            ServiceInstance product = loadBalancerClient.choose("PRODUCT");
            String url=String.format("http://%s:%s",product.getHost(),product.getPort()+"/product/list");
            List list = restTemplate.getForObject(url, List.class);
            return list.toString();
        }

### 第三种方式

配置RestTemplate组件，该方式与第二种方式一样，区别为添加了LoadBalancer注解，减少了冗余代码

    @Component
    public class RestTemplateConfig {
        @Bean
        @LoadBalanced
        public RestTemplate restTemplate(){
            return new RestTemplate();
        }
    }
    
在url中指定serviceId进行负载均衡调用

    @Autowired
    RestTemplate restTemplate;
    
    @GetMapping("/getProduct")
    public String getProduct(){
        List list = restTemplate.getForObject("http://PRODUCT/product/list", List.class);
        return list.toString();
    }
    
