package cloud.entity;


import lombok.Getter;

@Getter
public enum  InfoVer {
    ONE(1,"one"),
    TWO(2,"two");
    private Integer code;
    private String ver;

    InfoVer(Integer code, String ver) {
        this.code = code;
        this.ver = ver;
    }
}
