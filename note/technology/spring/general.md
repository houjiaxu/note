#常识
https://note.youdao.com/web/#/file/WEBdcc03127c1a4d9cd16dde229ebb621f3/note/WEBb1a023445c3f892e9866e560f83b2c19/

#Resource 接口

    提供了如下实现类：
    UrlResource：访问网络资源的实现类。
    ClassPathResource：访问类加载路径里资源的实现类。
    FileSystemResource：访问文件系统里资源的实现类。
    ServletContextResource：访问相对于 ServletContext 路径里的资源的实现类.
    InputStreamResource：访问输入流资源的实现类。
    ByteArrayResource：访问字节数组资源的实现类。

spring中的BeanFactory使用的是简单工厂模式,主要责任是负责生产bean

发布的时候,要先让机器上正在处理的请求运行完毕,也就是先让请求打到其他机器上, 然后再重启该机器的容器. 要不然其他请求过来直接报错.

在bean的销毁方法中getbean了,想这么做的话, spring有扩展点, 可以指定销毁前和销毁后要执行的逻辑
停止项目运行的时候也要打断点看下,整个流程时怎么样的.