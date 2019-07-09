package cloud.entity;

import lombok.Getter;

@Getter
public enum ResultEnum {

    PARAM(1,"参数错误");
    private Integer code;
    private String ver;

    ResultEnum(Integer code, String ver) {
        this.code = code;
        this.ver = ver;
    }
}
