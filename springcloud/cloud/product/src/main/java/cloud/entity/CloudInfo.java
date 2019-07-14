package cloud.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "cloud_info")
@Entity
public class CloudInfo {

    @Id
    private Integer id;
    private String name;
    private String ver;
}
