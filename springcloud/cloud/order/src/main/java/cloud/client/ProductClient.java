package cloud.client;

import cloud.entity.CloudInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("product")
public interface ProductClient {

    @GetMapping("/product/list")
    List<CloudInfo> productList();
}
