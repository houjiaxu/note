public class ProducerDemo {

    private static  KafkaProducer<String,String> producer;

    //产生消息
    public static void produceMessage(String topic,String message) {
        int messageNo = 1;
        final int COUNT = 5;
        while(messageNo < COUNT) {
//            String key = String.valueOf(messageNo);
            String data = String.format(message);
            try {
                ProducerRecord<String,String> record = new ProducerRecord<>(topic,data);
                producer.send(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
            messageNo++;
        }
        producer.close();
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "usdp-clustera-01:9092,usdp-clustera-02:9092,usdp-clustera-03:9092");//xxx服务器ip
        props.put("acks", "all");//所有follower都响应了才认为消息提交成功，即"committed"
        props.put("retries", 0);//retries = MAX 无限重试，直到你意识到出现了问题:)
        props.put("batch.size", 16384);//producer将试图批处理消息记录，以减少请求次数.默认的批量处理消息字节数
        //batch.size当批量的数据大小达到设定值后，就会立即发送，不顾下面的linger.ms
        props.put("linger.ms", 1);//延迟1ms发送，这项设置将通过增加小的延迟来完成--即，不是立即发送一条记录，producer将会等待给定的延迟时间以允许其他消息记录发送，这些消息记录可以批量处理
        props.put("buffer.memory", 33554432);//producer可以用来缓存数据的内存大小。
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
        produceMessage("test","test");
    }
}

