public class AmqpConfig {
    @Bean
    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(getSecondQueue(), getThirdQueue()); //监听的队列
        container.setConcurrentConsumers(1); // 最小消费者数
        container.setMaxConcurrentConsumers(5); // 最大的消费者数量
        container.setDefaultRequeueRejected(false); //是否重回队列
        container.setAcknowledgeMode(AcknowledgeMode.AUTO); //签收模式
        container.setExposeListenerChannel(true);
        container.setConsumerTagStrategy(new ConsumerTagStrategy() { //消费端的标签策略
            @Override
            public String createConsumerTag(String queue) {
                return queue + "_" + UUID.randomUUID().toString();
            }
        });
        return container;
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setAutoStartup(true);
        return factory;
    }

    //可以在消费者上指定，当我们需要监听多个 RabbitMQ 的服务器的时候，指定不同 的 MessageListenerContainerFactory。
    @Component
    @PropertySource("classpath:gupaomq.properties")
    @RabbitListener(queues = "${com.gupaoedu.firstqueue}", containerFactory = "rabbitListenerContainerFactory")
    public class FirstConsumer {
        @RabbitHandler
        public void process(@Payload Merchant merchant) {
            System.out.println("First Queue received msg : " + merchant.getName());
        }
    }


}