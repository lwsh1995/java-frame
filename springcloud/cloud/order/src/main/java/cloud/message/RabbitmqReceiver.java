package cloud.message;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqReceiver {

    //1. @RabbitListener(queues = "order")
    //2. 自动创建队列的方式 @RabbitListener(queuesToDeclare = @Queue("order"))
    //3.自动创建 ，Exchange和Queue绑定
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "myQueue"),
            key = "routingKey",
            exchange = @Exchange(value = "myExchange",type = "topic")
    ))
    public void process(String message){
        System.out.println("receiver: "+message);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "myQueueTwo"),
            key = "routingKeyTwo",
            exchange = @Exchange(value = "myExchange",type = "topic")
    ))
    public void processTwo(String message){
        System.out.println("receiver: "+message);
    }
}
