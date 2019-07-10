import cloud.dao.CloudInfoRepository;
import cloud.entity.CloudInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductRepositoryTest {
    @Autowired
    private CloudInfoRepository cloudInfoRepository;

    @Test
    public void list(){
        List<CloudInfo> byId = cloudInfoRepository.findById(1);
        System.out.println(byId.get(0).getName());
    }
}
