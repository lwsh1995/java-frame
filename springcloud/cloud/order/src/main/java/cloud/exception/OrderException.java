package cloud.exception;

public class OrderException extends RuntimeException {
    private Integer code;

    public OrderException(Integer code,String message) {
        super(message);
        this.code = code;
    }
}
