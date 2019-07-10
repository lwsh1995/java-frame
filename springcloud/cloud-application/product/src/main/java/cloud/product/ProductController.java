package cloud.product;

import cloud.dao.CloudInfoRepository;
import cloud.entity.CloudInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
