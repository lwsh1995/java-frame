package cloud.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class OrderInfo {
    @NotNull(message = "id不能为空")
    private Integer id;

    @NotNull(message = "name不能为空")
    private String name;

    @NotNull(message = "ver不能为空")
    private String ver;
}
