package cloud.controller;

import cloud.entity.CloudInfo;
import cloud.entity.OrderInfo;
import cloud.entity.ResultEnum;
import cloud.exception.OrderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

//    @Autowired
//    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/info")
    public String info(@Valid OrderInfo orderInfo, BindingResult bindingResult){

        if (bindingResult.hasErrors())
            throw new OrderException(ResultEnum.PARAM.getCode(),bindingResult.getFieldError().getDefaultMessage());

        return orderInfo.getName();
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
        List list = restTemplate.getForObject("http://PRODUCT/product/list", List.class);
        return list.toString();
    }
}
