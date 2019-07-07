### Bindings

Bindings可以采用额外的routingKey参数。为了避免与basic_publish参数混淆，将其称为绑定密钥。

    channel.queueBind(queueName，EXCHANGE_NAME，“black”);

bindings key决于exchange类型。之前使用的fanout exchange只是忽略了它的值。

### Direct exchange
如日志记录系统向所有worker广播所有消息。希望扩展它根据消息的严重性过滤消息。如需要一个程序将严重错误日志消息写入磁盘，而不是在

警告或详细信息日志消息上浪费磁盘空间。

fanout exchange没有太大的灵活性 - 它只能进行无意识的广播。

使用direct exchange消息进入队列，其binding key与消息的routing key完全匹配。

![img](https://www.rabbitmq.com/img/tutorials/direct-exchange.png)

在此设置中，Direct exchange的`X`与两个队列绑定。第一个队列绑定key `orange`，第二个队列有一个绑定`black`和一个绑定`green`。

使用routing key `orange`将被路由到队列Q1。routing kye 为`black`或`green`的消息将转到Q2。所有其他消息将被丢弃。

### Multiple bindings

![img](https://www.rabbitmq.com/img/tutorials/direct-exchange-multiple.png)

使用相同的binding key绑定多个队列是完全合法的。可以在X和Q1之间添加`black`的绑定。在这种情况下，direct和fanout效果一样，并将消
息广播到所有匹配的队列。`black`的消息将传送到 Q1和Q2。

### Emitting logs
将此模型用于日志系统。将消息发送给direct exchange而不是fanout。提供日志严重性作为routing key。接收程序将能够选择想要接收的严重

性。

    channel.exchangeDeclare(EXCHANGE_NAME,"direct");
    
送消息：

    channel.basicPublish(EXCHANGE_NAME,severity,null,message.getBytes());

假设“严重性”是`error`、`warning`、`info`。

### Subscribing

根据严重性创建一个新绑定

    String queueName = channel.queueDeclare().getQueue();
    channel.queueBind(queueName, EXCHANGE_NAME, severity);
![img](https://www.rabbitmq.com/img/tutorials/python-four.png)

`EmitLogDirect.java`

    public class EmitLogDirect {
        private static final String EXCHANGE_NAME="direct_logs";
        public static void main(String[] args) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME,"direct");
                for (int i = 0; i < 5; i++) {
                    channel.basicPublish(EXCHANGE_NAME, "error", null,("i "+i).getBytes());
                }
            }catch (Exception e){}
        }
    }

`ReceiveLogsDirect.java`

    public class ReceiveLogsDirect {
        private static final String EXCHANGE_NAME="direct_logs";
        public static void main(String[] args) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME,"direct");
                String queue = channel.queueDeclare().getQueue();
                channel.queueBind(queue,EXCHANGE_NAME,"error");
                channel.basicConsume(queue,true,(s, delivery) -> {
                    System.out.println(delivery.getEnvelope().getRoutingKey()+" "+new String(delivery.getBody()));
                },s -> {
                    System.out.println(s);
                });
            }catch (Exception e){}
        }
    }
