Redis为什么快?

    1.内存数据库。Redis的键值操作是基于内存的(而非基于磁盘)，内存的访问速度很快。
    2.高效的底层数据结构。Redis底层会用到压缩列表、跳表、哈希表等数据结构。
    3.高性能IO模型。Redis使用基于多路复用的高性能IO模型。
    4.处理命令是基于单线程多路复用的,即reactor模型.

基于磁盘和基于内存的区别

![](img/img_1.png)

![](img/img_2.png)


Redis支持的value类型有五种：String、List、Set、Sorted Set、Hash。
其底层数据结构大致可分为六种：简单动态字符串、双向链表、压缩列表、哈希表、跳表、数组。

    String：简单动态字符串
    List：双向链表、压缩列表
    Set：哈希表、数组
    Sorted Set：压缩列表、跳表
    Hash：压缩列表、哈希表
![](img/img.png)

[压缩列表](http://redisbook.com/preview/ziplist/list.html)

[redis持久化方式](https://blog.csdn.net/qq_34272760/article/details/123790139)

Redis进行持久化的时候会 fork了一个子进程 执行持久化操作

[redis持久化选择](https://www.cnblogs.com/dplog/p/15923157.html)

2种方式都开启,默认优先加载aof,用户也可以自由选择,如果是主从模式,可以加载RDB,然后去主节点同步数据就行了.

[redis延迟双删的策略](https://www.cnblogs.com/tiancai/p/15901210.html)

[分布式锁及问题](https://blog.csdn.net/Me_xuan/article/details/124418176)

[看门狗机制](https://www.cnblogs.com/jelly12345/p/14699492.html)
看门狗机制:每隔10s看下,如果还持有锁,则延长生存时间;默认情况下，看门狗的续期时间是30s，也可以通过修改Config.lockWatchdogTimeout来另行指定。
另外Redisson还提供了指定leaseTime参数来指定加锁的时间。超过这个时间后锁便自动解开了，不会延长锁的有效期。 
我感觉那其实还不如直接加个时间长的锁



[集群模式](https://www.jb51.net/article/224568.htm)
哨兵模式主要使用Raft选举算法

Sentinel将一个主服务器判断为主观下线之后，为了确认这个主服务器是否真的下线了，它会向同样监视这一主服务器的其他Sentinel进行询问，看它们是否也认为
主服务器已经进入了下线状态（可以是主观下线或者客观下线)。当Sentinel从其他Sentinel那里接收到足够数量的已下线判断之后，Sentinel就会将从服务器判
定为客观下线，并对主服务器执行故障转移操作。

故障转移,选举使用的是raft协议.
1.在已下线主服务器属下的所有从服务器里面，挑选出一个从服务器，并将其转换为主服务器。
2.让已下线主服务器属下的所有从服务器改为复制新的主服务器。
3.将已下线主服务器设置为新的主服务器的从服务器，当这个旧的主服务器重新上线时，它就会成为新的主服务器的从服务器。

[Redis 九种数据类型和应用场景！](https://mp.weixin.qq.com/s/PpaMttjOm2hsupw4R61anA)

[redis使用场景](https://mp.weixin.qq.com/s/2YqkwP-R3BAqh1kmhhW_AQ) 缓存,普通数据,分布式锁

[跳表1](https://blog.csdn.net/yjw123456/article/details/105159817/)

[跳表2](https://baijiahao.baidu.com/s?id=1710441201075985657&wfr=spider&for=pc)

![](img/img_3.png)

##问题

[redis的increase返回null](https://mp.weixin.qq.com/s/90Vwd7_03XRRDtIHWFE6Dw)
















