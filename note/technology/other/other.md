jdk中有哪些好用的Function接口?

在java.util.function中全都是:以下Function接口都有现成的子类型的实现

Supplier<T>  T get(); 没有参数,有返回

Function<T, R>  R apply(T t);  1个参数,有返回

BiFunction<T, U, R>   R apply(T t, U u);  2个参数,有返回

Consumer<T>  void accept(T t);   1个参数,无返回

BiConsumer<T, U>  void accept(T t, U u);  2个参数,无返回

Predicate<T>  boolean test(T t); 1个参数,返回boolean

BiPredicate<T, U>  boolean test(T t, U u); 2个参数,返回boolean






@JSONField 使用 https://blog.csdn.net/dmw412724/article/details/93761161

数据库分表:
    双写: 商家订单和用户订单,商家数据用商家id去分表，就能查到这个商家的所有订单
    使用组合键分表:比如把用户id（假如10）位，包含在订单号里(订单号: userid+雪花算法)。 分表时用userId做hash

@RequestMapping(value = "/customActivity/yangshipin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

例如秒杀100个商品 是不是可以循环添加到redis队列中 按照 1,2,3,4,5顺序 每次pop弹出 ，弹出值就是剩余量
这个也可以, 加个商品id前缀  比如  id_1   id_2, 取出的时候截取下. 这个可能更好,但是可能队列会有并发问题
先把结余值空着，扣完库存，事后再异步填上

spring进行封装的

StringRedisTemplate <String, String>
RedisTemplate<Object, Object>
RedisAtomicClient(redisTemplate),其实还是redisTemplate, 内部是锁

EasyExcel 阿里的excel导出功能

redis:
    ZSet    add(key,  value, score)  score 是分值, 可以查分值
            rangeByScoreWithScores(key, minScore, maxScore)  查找 minScore <= score <= maxScore 的数据条数

WebMvcConfigurer: 可用来添加额外的拦截器/参数解析器等等

com.google.common.util.concurrent.RateLimiter  谷歌的令牌桶,用来做单机限流

简单的一次http请求包含的内容:TCP三次握手 -> http请求 -> http响应 -> TCP四次挥手
现在的http请求可能包含: TCP三次握手 -> 重复多次,一个html页面包含图片/css/js等文件(http请求 -> http响应) -> TCP四次挥手
持久链接: 只要任意一端 没有明确提出断开连接，则保持 TCP 连接状态。

unwrap-展开-父类型转换成子类型

controller直接注入rpc的service,理论上service是个接口,但是为什么能够进行注入呢? 原理好像是什么spi机制还是啥玩意来着,忘求了
RequestLocal类要看一下
spring中每个包里面的功能,特别是web包里的

