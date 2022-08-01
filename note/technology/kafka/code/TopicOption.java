public class TopicOption {
//    String zk =new GetProperties("");

    private static final ZkUtils ZK = ZkUtils.apply("usdp-clustera-01:2181,usdp-clustera-02:2181,usdp-clustera-03:2181",3000,3000, JaasUtils.isZkSecurityEnabled());

    public final static String topic = "test";

    public static void main(String[] args) {
        createTopic();
//        changeTopicPartitions("2");
    }

    public TopicOption() throws IOException {
    }


    //获取topic list,通过zk
    @Test
    public void getTopicList(){
        List<String> topiclist= JavaConversions.seqAsJavaList(ZK.getAllTopics());
        System.out.println(topiclist);
    }

    //创建topic
    @Test
    public static void createTopic(){
        AdminUtils.createTopic(ZK,topic,1,1,new Properties(),
                RackAwareMode.Enforced$.MODULE$);
    }

    //添加分区
    @Test
    public static void changeTopicPartitions(int partitions){
        AdminUtils.addPartitions(ZK,topic,partitions,"",true,RackAwareMode.Enforced$.MODULE$);
    }

    //查看topic详情
    @Test
    public void queryTopic(){
        Properties prop = AdminUtils.fetchEntityConfig(ZK, ConfigType.Topic(),"__consumer_offsets");
        Iterator it = prop.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            Object key =entry.getKey();
            Object value =entry.getValue();
            System.out.println(key + "=" + value);
        }
    }
    //删除topic
    @Test
    public void deleteTopic(){
        AdminUtils.deleteTopic(ZK,topic);
    }

    //主体是否存在
    @Test
    public void topicExists(){
        boolean exists = AdminUtils.topicExists(ZK,topic);
        System.out.println(exists);
    }

    @After
    public void closeZk(){
        ZK.close();
    }

}



























