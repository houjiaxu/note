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

通用excel导出功能:正常情况下开发人员是不需要这个功能的,开发人员申请个数据库权限,查个sql就能导出数据,这种功能是针对客户的.

    1.工具:EasyExcel 阿里的excel导出功能
    2.根据sqlId进行导出,或者直接传进去一个sql进行导出
    3.注意点: 
         3.1 所执行的sql必须只能包含select, 不能包含delete update insert等语句
         3.2 必须做权限的校验,只有哪些账号才能做导出
         3.3 每次导出必须有记录,谁,sql/sqlid,时间
         3.4 导出时要做签名.

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



分布式id

    uuid(uuid有5个版本)
        版本1: 时间+mac地址
        版本2: 对版本1进行了改造,使用了dce算法,保证mac不被泄露
        版本3: 基于字符的MD5算法, hash, 会有hash冲突
        版本4: 随机的UUID  大概几百年左右会重复1次
        版本5: 基于sha1算法
    java.util.UUID类中使用了2个版本的UUID
        版本3: MD5
        版本4: 是个16进制,128位二进制转换成的32位的16进制,然后加上4个"-",一共是36位.   8位-4位-Mmmm-4-12, 中的M代表版本号
    机器码+数据库自增
    数据库自增(步长)
    redis 自增(redis是命令执行单线程,无需考虑并发),还可以使用incre + 时间戳拼接
    zk/mongdb都和redis差不多, 但是费钱,搭redis是要机器的.
    雪花算法: 64位二进制   1bit(符号位) + 41位的时间戳(毫秒时间  系统时间 - 系统上线时间) + 10位的机器id(唯一标志) + 12位的序列号(递增)
    改造版:leaf-agement  leaf-snowflake
    美团的leaf, 百度uidgenerator