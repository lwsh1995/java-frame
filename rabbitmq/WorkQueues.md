### Work Queues
工作队列用于在多个工作人员之间分配耗时的任务。
工作队列（任务队列）主要是避免立即执行资源密集型任务，并且必须等待它完成。相反安排任务稍后完成。将任务封装为消息并将其发送
到队列。在后台运行的工作进程将弹出任务并最终执行作业。当运行许多工作程序时，它们之间将共享任务。

### 准备
建立任务生产者 `NewTask.java`

    public class NewTask implements Runnable{
        public static String QUEUE_NAME="work";
        public static void main(String[] args) {
            Thread consumer1 = new Thread(new NewTask());
            consumer1.setName("p1");consumer1.start();
            Thread consumer2 = new Thread(new NewTask());
            consumer2.setName("p2");consumer2.start();
        }
        public void run() {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                for (int i = 0; i < 5; i++) {
                    String message = Thread.currentThread().getName()+" "+ i + " test...";
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                }
            } catch (Exception e) {}
        }
    }

建立任务工作者 `Worker.java`

    public class Worker implements Runnable {
        private static String QUEUE_NAME = "work";
        public static void main(String[] args) {
            Thread work1 = new Thread(new Worker());
            work1.setName("work 1");work1.start();
            Thread work2 = new Thread(new Worker());
            work2.setName("work 2");work2.start();
        }
        public void run() {
            try {
                ConnectionFactory connect = new ConnectionFactory();
                connect.setHost("localhost");
                Connection connection = connect.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                DeliverCallback deliverCallback = (s, deliver) -> {
                    String s1 = new String(deliver.getBody(), "utf-8");
                    try {
                        doWorkd(s1);
                    } catch (Exception e) {
                    } finally {
                        System.out.println(Thread.currentThread().getName() + " " + s1 + " done");
                    }
                };
                CancelCallback cancelCallback = (s) -> {
                    System.out.println(s);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);
            } catch (Exception e) {
            }
        }
        public static void doWorkd(String task) throws Exception {
            for (char ch : task.toCharArray())
                if (ch == '.');
        }
    }
    
   模拟2个worker消费以及两个product生产
```$xslt
pool-1-thread-4 p1 1 test... done
pool-2-thread-4 p1 0 test... done
pool-1-thread-4 p1 3 test... done
pool-2-thread-4 p1 2 test... done
pool-1-thread-4 p2 0 test... done
pool-2-thread-4 p1 4 test... done
pool-2-thread-4 p2 1 test... done
pool-1-thread-5 p2 2 test... done
pool-2-thread-5 p2 3 test... done
pool-1-thread-5 p2 4 test... done
```
RabbitMQ默认按顺序发送消息到下一个worker，每个worker将获得相同数量的的消息，称为round-robin dispatching循环分配法。

### message ack (消息确认)
执行任务需要时间。如果其中一个worker开始一项长期任务并且只是部分完成而死亡。使用当前代码，一旦RabbitMQ向worker发送消息，它立即

将其标记为删除。在这种情况下，如果kill worker，将丢失它刚刚处理的消息。还将丢失分发给这个特定工作者但尚未处理的所有消息。

如果不失去任何任务。如果worker死亡，将任务交付给其他worker。

为了确保消息永不丢失，RabbitMQ支持消息确认。worker发回ack告诉RabbitMQ已收到，处理了特定消息，RabbitMQ可以自由删除它。

如果worker死亡（其通道关闭，连接关闭或TCP连接丢失）而不发送确认，RabbitMQ将理解消息未完全处理并将重新排队。如果同时有其他在线

worker，则会迅速将其重新发送给其他worker。就可以确保没有消息丢失，即使worker偶尔会死亡。

当worker死亡时，RabbitMQ将重新发送消息。没有任何消息超时，即使处理消息需要非常长的时间，也正常工作。

默认情况下，手动消息确认已打开。前面的例子通过autoAck = true 标志明确地将它们关闭。现在将此标志设置为false，完成任务从worker发送正确的确认。

    channel.basicQos(1);
    DeliverCallback deliverCallback = (s, deliver) -> {
        String s1 = new String(deliver.getBody(), "utf-8");
        try {
            doWorkd(s1);
        } catch (Exception e) {
        } finally {
            System.out.println(Thread.currentThread().getName() + " " + s1 + " done");
            channel.basicAck(deliver.getEnvelope().getDeliveryTag(),false);
        }
    };
    CancelCallback cancelCallback = (s) -> {
        System.out.println(s);
    };
    boolean autoAck=false;
    channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, cancelCallback);
使用此设置，即使在处理消息时杀死worker，也不会丢失任何内容。所有未经确认的消息将被重新传递。

```$xslt
遗忘确认
一个公共的错误是丢失 basicAck，这是简单的错误，但后果很严重。当client退出消息会重发，如果没有释放任何未确认的消息，RabbitMQ将
会吃掉越来越多的内存，可使用rabbitmqctl打印 messages_unacknowledged 字段
rabbitmqctl list_queues name messages_ready messages_unacknowledged
```
### message durable (消息持久性)
RabbitMQ永远丢失队列。需要声明持久的,RabbitMQ不允许使用不同的参数重新定义现有队列，并将向尝试执行此操作的任何程序返回错误。

     boolean durable=true;
     channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
此queueDeclare更改需要应用于生产者和消费者代码
     
     hannel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
将消息标记为持久性 - 通过将MessageProperties（实现BasicProperties）设置为值PERSISTENT_TEXT_PLAIN。

```$xslt
将消息标记为持久性并不能完全保证消息不会丢失。虽然RabbitMQ将消息保存到磁盘，
但是有一个短时间窗口RabbitMQ接受消息并且尚未保存消息时。此外，RabbitMQ不会
为每条消息执行fsync（2） - 它可能只是保存到缓存而不是真正写入磁盘。持久性保
证不强。如果需要更强的保证，可以使用 publisher confirms发布者确认。
```

### fair dispatching (公平派遣)
公平派遣没有达到预期的效果。无论消息是否很重或者很轻，worker是否繁忙或者工作，RabbitMQ一直保持均匀的分派消息。
RabbitMQ在消息进入队列调度消息。它不会查看worker未确认消息的数量。只是盲目地向第n个消费者发送每个第n个消息。
可以使用basicQos方法和 prefetchCount = 1设置。RabbitMQ在处理并确认前一个消息之前，不向worker发送新消息。

     channel.basicQos(1);
```$xslt
队列大小
如果所有worker都很忙，队列就会填满。需要并可能添加更多工作人员，或者采取其他策略。
```
