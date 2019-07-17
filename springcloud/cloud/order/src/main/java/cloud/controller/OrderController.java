package cloud.controller;

import cloud.client.ProductClient;
import cloud.entity.CloudInfo;
import cloud.entity.OrderInfo;
import cloud.entity.ResultEnum;
import cloud.exception.OrderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/order")
@RefreshScope
public class OrderController {

//    @Autowired
//    LoadBalancerClient loadBalancerClient;

//    @Autowired
//    RestTemplate restTemplate;

    @Autowired
    ProductClient productClient;

    @Value("${env}")
    private String env;

    @PostMapping("/info")
    public String info(@Valid OrderInfo orderInfo, BindingResult bindingResult){

        if (bindingResult.hasErrors())
            throw new OrderException(ResultEnum.PARAM.getCode(),bindingResult.getFieldError().getDefaultMessage());

        return orderInfo.getName();
    }

    @GetMapping("/create")
    public String create(){
        return "create";
    }

    @GetMapping("finish")
    public String finish(){
        return "finish";
    }

    @GetMapping("list")
    @CrossOrigin(allowCredentials = "true")
    public String list(){
        return "list";
    }

    @GetMapping("/getProduct")
    public String getProduct(){
        //1、第一种方式（直接写死url）
//        RestTemplate restTemplate=new RestTemplate();
//        List list = restTemplate.getForObject("http://localhost:8701/product/list", List.class);

        //2、第二种方式（LoadBalancer获取应用名的url，在使用RestTemplate）
//        RestTemplate restTemplate=new RestTemplate();
//        ServiceInstance product = loadBalancerClient.choose("PRODUCT");
//        String url=String.format("http://%s:%s",product.getHost(),product.getPort()+"/product/list");
//        List list = restTemplate.getForObject(url, List.class);

        //3、第三种方式（利用@LoadBalancer，在url中使用应用名）
//        List list = restTemplate.getForObject("http://PRODUCT/product/list", List.class);

        List<CloudInfo> list = productClient.productList();
        return list.toString();
    }

    @GetMapping("/env")
    public String env(HttpServletRequest request){
        return env;
    }
}
