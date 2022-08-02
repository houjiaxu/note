public class DelayQueue {
    @Bean("delayExchange")
    public TopicExchange exchange() {
        Map<String, Object> argss = new HashMap<String, Object>();
        argss.put("x-delayed-type", "direct");
        return new TopicExchange("GP_DELAY_EXCHANGE", true, false, argss);
    }

//生产者:
    //消息属性中指定 x-delay 参数。
    MessageProperties messageProperties = new MessageProperties();
    // 延迟的间隔时间，目标时刻减去当前时刻
    messageProperties.setHeader("x-delay",delayTime.getTime()-now.getTime());
    Message message = new Message(msg.getBytes(), messageProperties);

    // 不能在本地测试，必须发送消息到安装了插件的 Linux 服务端
     rabbitTemplate.send("GP_DELAY_EXCHANGE","#",message);
}















