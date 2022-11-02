[md使用方式](https://www.cnblogs.com/xihailong/p/13919914.html)

[芋道源码解析网站](https://www.iocoder.cn/)

[大佬blog](https://blog.csdn.net/qq_19414183?type=blog)

[美团技术团队文章](https://tech.meituan.com/)

[阿里云开发社区](https://developer.aliyun.com/)

[敖丙](https://github.com/AobingJava/JavaFamily)

[大促案例](https://mp.weixin.qq.com/s/TmohlJEDdi92DSzHWRazFQ)

[skywalking](https://mp.weixin.qq.com/s/3ONVrA2_UmM9qbOPdGOrxA)

[CountDownLatch和CyclicBarrier](https://www.jianshu.com/p/043ac5689002)
主要是CountDownLatch只调用一次await,而CyclicBarrier调用多次await,CountDownLatch基于abq同步阻塞队列, CyclicBarrier基于ReentrantLock Condition

[CountDownLatch和CyclicBarrier区别](https://blog.csdn.net/wl_ang/article/details/104922464)

CountDownLatch和CyclicBarrier区别:

    CountDownLatch各线程都准备完毕,然后开始执行某个事情
    CyclicBarrier让一组线程到达某个屏障，然后被阻塞，一直到最后一个线程到达屏障，然后屏障开放，所有被阻塞的线程继续执行。
    CountDownLatch的计数器是大于或等于线程数的，而CyclicBarrier是一定等于线程数
    CyclicBarrier的计数器可以重置

[软件架构设计入门学习](https://blog.csdn.net/qq_40664711/article/details/123044648)

[架构书籍](https://zhuanlan.zhihu.com/p/436294029?utm_medium=social&utm_oi=984892880510365696)

[架构书籍](https://blog.csdn.net/sunyufeng22/article/details/120562588)

[架构书籍](https://blog.csdn.net/hzbooks/article/details/125713741)


![](NPC问题.jpg)

install本地 https://www.cnblogs.com/z_lb/p/12673728.html

长期的职业规划, netty和juc以及spring必须学透，netty和juc是做中间件和高性能组件必须的技能，spring吃透的话就很容易整合进去，方便使用

总结一下框架原理,然后实际应用中的配置值,框架的对比.

数据库分片用的哪个工具? 木有分片
公司对rocketmq的扩展,都扩展了什么? 加了多场景

unsafe类的compareAndSwap是原语,原语是指由若干条指令组成的程序段，在执行过程中不可被中断。即保证了原子性。


为单独微服务开发定制网关，做到某个活动某个接口细粒度限流?
[SpringBoot细粒度、可扩展的接口限流实现](https://blog.csdn.net/qq_41310634/article/details/119935776)

接口细粒度限流自定义实现:

    方案一:前公司的实现是在网关进行拦截,利用本地缓存caffeine框架来做计数器,实现算法基本按照计数器进行计数.
    方案二:在网关使用redis根据url统计,自然秒内有多少请求访问了
        increase后的值大于等于设置的阈值时,就触发限流,返回特定页面
        redis里的值每秒过期,或者是使用url+时分秒作为key,过期时间可以设置长一些

你来设计一个mq or 注册中心,你会怎么设计?

    主旨:除基本功能外,还要高可用.

你印象中最难的问题是什么?

    这个问题无论在技术还是业务都要有一个.
    数据库死锁问题的排查: 高并发场景下,操作同一条数据导致死锁. 具体: a,b,c3个线程操作同一条数据,a先独占锁,在innodb引擎内部会把b,c此时会转换成读锁,
        此时a线程回滚,b和c开始抢占数据加独占锁,但由于b,c已经加了读锁,导致独占锁加不上,故此产生死锁.
    中意人寿的集步: 行走方式6中,8种不同的障碍道具,跑的过程中3种奖励(能量补给站,权益,金币), 遇到加减路程之后3种奖励以什么样的方式计算. 终评之后,另一个一起做的同事直接说hold不住. 
        我:初步分析,找同事详细过一遍,完善技术方案, 开发. 测试, 我这边是没有bug的,倒是前端出了不少bug.
    代项目经理的协调: 有些人是有情绪的,不愿做,一方面需要和该同事说明情况, 一方面跟部门主管协调人.

最让你感觉到自豪or最有成就感的是什么?
    
    做一些大家都能用的东西,比如组件,代码模板,后来给工具组提过根据技术方案生成代码
    做mvp.
    codereview等
    银通项目的模块重构,重新梳理那块的业务,设计方案,和主管同事过技术方案,然后进行开发,开发完毕之后进行测试. 不同的业务有不同的定制化处理. 
        更好的代码实现方式:在每一步主要步骤的前后都加上postProcessor,然后当做模板方法,子类可以实现.
你的优势是什么?
    
    工作经验:比较丰富,不论是传统的项目还是高并发项目都有做过, 也做过微服务等;
    性格: 逻辑比较强,提测bug非常少;
    心态也比较好:做这行的有时候压力会比较大,所以心态比较好也算是一个优势;
    没对象: 有大把的时间学习,离职之前是准备看源码,最近是看了些面试相关的东西;

读书or看源码最大的体会是什么?

    1.知道了原理
    2.提炼出了一些思想:
        比如防止并发写eureka是通过多级缓存,nacos通过copyOnWrite,
        再比如组件之间的设计和整合,例如nacos整合ribbon是通过实现ribbon的serverlist接口,这么设计不合理,应该在springcloudcommon设计一个接口,让nacos去实现.
    3.设计模式的应用

自己的方法论
    
做需求：根据prd一步一步解析，当前需要用到什么道具、表，涉及哪些接口、需要返回什么值、一步一步的罗列出来，然后再整合到技术方案上，编码的方式是先写逻辑，
    当然，我有自己的代码模板，写完代码再考虑其他的token校验、事务、并发锁、日志等这些注解要不要添加，因为codereview的时候，review的就是这些东西，最后再自测联调。


WebMvcConfigurerAdapter 这个玩意到底在哪里调用的,而且这个好像可以有多个实现类,里面的拦截器是&&的关系,springboot2.0之后推荐直接实现WebMvcConfigurer或者直接继承WebMVCConfigurationSupport.

[mybatis-plus简单使用](https://mp.weixin.qq.com/s/N5htFZ-pEOfAOi9ZZUebZQ)

[深入理解ReentrantLock Condition](https://www.jianshu.com/p/1014fdd375cf)

[springcloud源码剖析](https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzAwMjI0ODk0NA==&action=getalbum&album_id=2083392961806925826&scene=173&from_msgid=2451964154&from_itemidx=1&count=3&nolastread=1#wechat_redirect)


[高可用](https://mp.weixin.qq.com/s/MQF0VtuNqWPKMeOVdpwbfA)
[高可用haproxy+keepalived](https://blog.csdn.net/m0_50019871/article/details/109751090)


[替代ELK：ClickHouse+Kafka+FlieBeat](https://mp.weixin.qq.com/s/2nMYKby1YOWilPRBHhKrDA)


eureka构建项目出现异常: Task with name 'postRelease' not found in root project 'eureka'.
在文件夹双击根目录的gradlew.bat文件,微信截图能看到原因,然后调整相应的错误即可,不过双击的时候通常.bat文件执行的很快,会一闪而过,
这时候我们只要按Ctrl + c即可

后面发现其实人家源码好像没问题,只不过我下载源码的方式是直接下载了zip文件,如果直接git clone就没问题了.

1.将build.gradle里的    id 'nebula.netflixoss' version '9.1.0' 版本号往下该即可,我是改成了'5.1.0'


[Spring Boot + @Async ](https://mp.weixin.qq.com/s/3L74TWepgzHty-s4ZAng2g)


3个数据，插入第4个，插入到索引1，那么原索引1-2的数据要移动，原索引3的要放入第2个。


[快速排序算法](https://blog.csdn.net/weixin_43586713/article/details/119820797)

各种心得要进行总结，比如数据表设计，遵循3范式，状态用tinyint表示等等。

https://mp.weixin.qq.com/s/nj9tnz1JI3ZsLeuk8S5DTg