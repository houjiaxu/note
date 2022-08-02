public class DlxConfig {
    //1、声明原交换机（GP_ORI_USE_EXCHANGE）、原队列（GP_ORI_USE_QUEUE），相互绑定。
    @Bean("oriUseExchange")
    public DirectExchange exchange() {
        return new DirectExchange("GP_ORI_USE_EXCHANGE", true, false, new HashMap<>());
    }

    @Bean("oriUseQueue")
    public Queue queue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x-message-ttl", 10000); // 10 秒钟后成为死信
        map.put("x-dead-letter-exchange", "GP_DEAD_LETTER_EXCHANGE"); // 队列中的消息变成死信后，进入死信 交换机
        return new Queue("GP_ORI_USE_QUEUE", true, false, false, map);
    }

    @Bean
    public Binding binding(@Qualifier("oriUseQueue") Queue queue,
                           @Qualifier("oriUseExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("gupao.ori.use");
    }

    //2 、 声 明 死 信 交 换 机 （ GP_DEAD_LETTER_EXCHANGE ） 、 死 信 队 列 （GP_DEAD_LETTER_QUEUE），相互绑定
    @Bean("deatLetterExchange")
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("GP_DEAD_LETTER_EXCHANGE", true, false, new HashMap<>());
    }

    @Bean("deatLetterQueue")
    public Queue deadLetterQueue() {
        return new Queue("GP_DEAD_LETTER_QUEUE", true, false, false, new HashMap<>());
    }

    @Bean
    public Binding bindingDead(@Qualifier("deatLetterQueue") Queue queue,
                               @Qualifier("deatLetterExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("#"); // 无条件路由
     }
    //3、最终消费者监听死信队列。
        //建个消费者监听deatLetterQueue即可
    //4、生产者发送消息。
}
