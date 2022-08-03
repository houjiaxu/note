public class TemplateConfig {

    //ConfirmCallBack
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            }
        });
        return rabbitTemplate;
    }
    //ReturnCallBack
    rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){
        public void confirm (CorrelationData correlationData,boolean ack, String cause){
            if (ack) {
                System.out.println("消息确认成功");
            } else { // nack
                System.out.println("消息确认失败");
            }
        }
    });

}