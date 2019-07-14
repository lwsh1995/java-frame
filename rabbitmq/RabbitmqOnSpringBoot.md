### rabbitmq 在springboot中使用

引入maven
    
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-amqp</artifactId>
            </dependency>
            
配置监听类

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
    

