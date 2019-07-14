### Stream

对消息中间件的封装，动态切换中间件

引入maven依赖

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
        <version>2.1.2.RELEASE</version>
    </dependency>
    
application.yml引入配置，避免启动多个节点重复消费一条消息

    spring:
      cloud:
        stream:
          bindings:
            queueName: #队列名
              group: order #应用名
              content-type: application/json #配置消息的序列化格式，可在rabbitmq中可以查看
              
接口类

    import org.springframework.cloud.stream.annotation.Input;
    import org.springframework.cloud.stream.annotation.Output;
    import org.springframework.messaging.MessageChannel;
    import org.springframework.messaging.SubscribableChannel;
    public interface StreamClient {
        String INPUT ="INPUT";
        String OUTPUT="OUTPUT";
        @Input(StreamClient.INPUT)
        SubscribableChannel input();
        @Output(StreamClient.OUTPUT)
        MessageChannel output();
    }

监听类，StreamListener用于监听一个队列，SendTo为向一个队列发送一个消息，可以组合使用

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

使用类

    @RestController
    public class StreamSendMessageControlelr {
    
        @Autowired
        private StreamClient streamClient;
        @GetMapping("/send")
        public void process(){
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(1);
            orderInfo.setName("order");
            orderInfo.setVer("one");
            streamClient.output().send(MessageBuilder.withPayload(orderInfo).build());
        }
    }