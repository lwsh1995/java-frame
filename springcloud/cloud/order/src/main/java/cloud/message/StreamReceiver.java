package cloud.message;

import cloud.entity.OrderInfo;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(StreamClient.class)
public class StreamReceiver {

    @StreamListener(value = StreamClient.INPUT)
    @SendTo(StreamClient.OUTPUT)
    public Object processInput(Object message){
        System.out.println("input queue  "+message);
        return "input to output "+message;
    }

    @StreamListener(StreamClient.OUTPUT)
    public void processOutput(OrderInfo message){
        System.out.println("output queue "+message);
    }
}
