package cloud.controller;

import cloud.entity.OrderInfo;
import cloud.message.StreamClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
