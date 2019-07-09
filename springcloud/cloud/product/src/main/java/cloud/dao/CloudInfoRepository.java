package cloud.dao;

import cloud.entity.CloudInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CloudInfoRepository extends JpaRepository<CloudInfo,String> {
    List<CloudInfo> findById(int id);
}
