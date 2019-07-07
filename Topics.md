###   Topic exchange

Direct仍有局限性，不能基于多个标准路由。日志系统不仅根据严重性订阅日志，还根据日志的源订阅。

发送到Topic exchange的消息不能具有任意的routing_key - 必须是由点分隔的单词列表。通常它们指定与消息相关的一些功能。routing key

示例：`stock.usd.nyse`、`nyse.vmw`、`quick.orange.rabbit`。routing key中可以包含单词数最多可达255个字节。

binding key也必须采用相同的形式。topic exchange使用特定routing key 发送的消息将被传递到与匹配的binding key绑定的所有队列。但

是binding key有两个重要的特殊情况：

+ `*` 可以替代一个单词。
+ `#` 可以替换零个或多个单词。

![img](https://www.rabbitmq.com/img/tutorials/python-five.png)

上图中将发送所有描述动物的消息。消息将与包含routing key一起发送。routing key的第一个单词描述速度，第二个颜色，第三个物种：
 `<speed>.<color>.<species>`。

创建三个bindings：Q1绑定了绑定键`*.orange.*`，Q2 绑定了`*.*.rabbit`和`lazy.#`。

+ Q1对所有orange感兴趣。
+ Q2接收rabbit和lazy的动物。

routing key设置为`quick.orange.rabbit`的消息将传递到两个队列。消息`lazy.orange.elephant`也将同时发送两个队列。另一方面，

`quick.orange.fox`只会转到第一个队列，而`lazy.brown.fox`只会转到第二个队列。`lazy.pink.rabbit`将仅传递到第二个队列一次，

即使它匹配两个bindings。`quick.brown.fox`与任何bindings都不匹配，因此它将被丢弃。

如果违反合约并发送带有一个或四个单词的消息，例如`orange`或`quick.orange.male.rabbit`，这些消息将不匹配任何绑定，并将丢失。

`lazy.orange.male.rabbit`即使它有四个单词，也会匹配最后一个lazy开头的bindings，并将被传递到第二个队列。

```
    topic exchange
    
    当队列绑定“ # ”（哈希）bing key时 - 它将接收所有消息，而不管routing key - 如fanout。
    
    当特殊字符“ * ”（星号）和“ ＃ ”（哈希）未在bindings中使用时，topic的行为就像direct一样。
```

`EmitLogTopic.java`

    public class EmitLogTopic {
        private static final String EXCHANGE_NAME="topic_logs";
        public static void main(String[] args) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME,"topic");
                for (int i = 0; i < 5; i++) {
                    channel.basicPublish(EXCHANGE_NAME, "var.web", null,("i "+i).getBytes());
                }
            }catch (Exception e){}
        }
    }
    
`ReceiveLogsTopic.java`

    public class ReceiveLogsTopic {
        private static final String EXCHANGE_NAME="topic_logs";
        public static void main(String[] args) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME,"topic");
                String queue = channel.queueDeclare().getQueue();
                channel.queueBind(queue,EXCHANGE_NAME,"*.web");
                channel.basicConsume(queue,true,(s, delivery) -> {
                    System.out.println(delivery.getEnvelope().getRoutingKey()+" "+new String(delivery.getBody()));
                },s -> {
                    System.out.println(s);
                });
            }catch (Exception e){}
        }
    } 
