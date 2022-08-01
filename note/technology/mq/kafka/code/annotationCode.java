//生产者
@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send() {
        kafkaTemplate.send("test", "msgKey", "msgData");
    }
}

//消费者
@Component
public class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    public void listener(ConsumerRecord record) {
        Optional<?> msg = Optional.ofNullable(record.value());
        if (msg.isPresent()) {
            System.out.println(msg.get());
        }
    }
}

