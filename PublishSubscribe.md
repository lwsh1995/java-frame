### Exchanges(交易所)

RabbitMQ中消息传递模型的核心思想是生产者永远不会将任何消息直接发送到队列。实际上生产者通常甚至不知道消息是否会被传递到任何队列。

生产者只能向Exchanges发送消息。一方面，它接收来自生产者的消息，另一方面将它们推送到队列。Exchanges必须确切知道如何处理它收到的

消息。它应该附加到特定队列吗？它应该附加到许多队列吗？或者它应该被丢弃。其规则由交换类型定义 。

有几种交换类型可供选择：direct，topic，headers，fanout。创建fanout类型的交换，并将其称为log：

    channel.exchangeDeclare("logs","fanout");

fanout只是将收到的所有消息广播到它知道的所有队列中。而这正是logger需要的。

```$xslt
    列出 exchanges
    rabbitmqctl list_exchanges
    Nameless exchanges
    在WorkQueue中，没有指定exchanges仍可向队列发送消息，使用了默认的exchanges，通过空字符串""识别
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
```
现在发布到命名的exchanges

    channel.basicPublish("logs", QUEUE_NAME, null, message.getBytes());
    
### Temporary queues (临时队列)

logger需要所有的log信息，不只是其中一部分。每当连接Rabbit需要新的空队列，可以使用随机的队列名称，或者让服务器选择随机队列名。

其次，一旦断开就应该自动删除队列。

在Java客户端中，当没有向queueDeclare()提供参数时，使用生成的名称创建一个非持久的，独占的自动删除队列：如amq.gen-JzTY20BRgKO-HjmUJj0wLg

    String queueName = channel.queueDeclare().getQueue();

Bindings

![text](https://www.rabbitmq.com/img/tutorials/bindings.png)

创建了fanout exchange和queue，需要告诉exchange发送消息到queue，在exchange和queu的关系叫binding

    channel.queueBind(queueName,"logs","");
    
```$xslt
    列出bindings
    rabbitmqctl list_bindings
```

现在将消息发布到logs exchange而不是nameless exchange。需要在发送时提供routingKey，但是对于fanout，它的值会被忽略。
`EmitLog.java`：

    public class EmitLog {
        private static String EXCHANGE_NAME="logs";
        public static void main(String[] args) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
                for (int i = 0; i < 5; i++) {
                    String str=" i "+i;
                    channel.basicPublish(EXCHANGE_NAME, "", null,str.getBytes());
                }
            }catch (Exception e){}
        }
    }
`ReceiveLogs.java`：

    public class ReceiveLogs implements Runnable{
        private static String EXCHANGE_NAME="logs";
        public static void main(String[] args) {
            new Thread(new ReceiveLogs()).start();
            new Thread(new ReceiveLogs()).start();
        }
        public void run() {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, "");
                channel.basicConsume(queueName, true, (s, delivery) -> {
                    String message = new String(delivery.getBody(),"utf8");
                    System.out.println(Thread.currentThread().getName() + " " + message);
                }, s -> {
                    System.out.println(s);
                });
            }catch (Exception e ){}
        }
    }
```$xslt
    pool-2-thread-4  i 0
    pool-2-thread-4  i 1
    pool-1-thread-4  i 0
    pool-2-thread-4  i 2
    pool-2-thread-4  i 3
    pool-1-thread-4  i 1
    pool-1-thread-4  i 2
    pool-2-thread-5  i 4
    pool-1-thread-5  i 3
    pool-1-thread-5  i 4
```
