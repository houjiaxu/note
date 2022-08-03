public class SaveQueue {
    //1、队列持久化
    @Bean("GpQueue")
    public Queue GpQueue() {
        // queueName, durable, exclusive, autoDelete, Properties
        return new Queue("GP_TEST_QUEUE", true, false, false, new HashMap<>());
    }

    //2、交换机持久化
    @Bean("GpExchange")
    public DirectExchange exchange() {
        // exchangeName, durable, exclusive, autoDelete, Properties
        return new DirectExchange("GP_TEST_EXCHANGE", true, false, new HashMap<>());
    }

    //3、消息持久化
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
    Message message = new Message("持久化消息".getBytes(), messageProperties);
    rabbitTemplate.send("GP_TEST_EXCHANGE","gupao.test",message);
}