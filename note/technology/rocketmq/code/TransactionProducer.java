/***
 * a、需创建事务类型的生产者TransactionMQProducer；
 * b、需调用setTransactionListener()方法设置事务监听器；
 * c、使用sendMessageInTransaction()以事务方式发送消息
 */
public class TransactionProducer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        // 创建事务类型的生产者
        TransactionMQProducer producer = new TransactionMQProducer("transaction-producer-group");
        // 设置NameServer的地址
        producer.setNamesrvAddr("10.0.90.211:9876");
        // 设置事务监听器
        producer.setTransactionListener(new TransactionListenerImpl());
        // 启动生产者
        producer.start();

        // 发送10条消息
        for (int i = 0; i < 10; i++) {
            try {
                Message msg = new Message("TransactionTopic", "", ("Hello RocketMQ Transaction Message" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 设置消息Key
                msg.setKeys("Num" + i);
                // 使用事务方式发送消息
                SendResult sendResult = producer.sendMessageInTransaction(msg, null);
                System.out.println("sendResult = " + sendResult);
                Thread.sleep(10);
            } catch (MQClientException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // 阻塞，目的是为了在消息发送完成后才关闭生产者
        Thread.sleep(10000);
        producer.shutdown();
    }
}