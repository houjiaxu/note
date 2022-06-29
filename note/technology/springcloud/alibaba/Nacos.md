注册instance是存储在哪里?
    
    临时节点存储在内存中,持久化节点持久化到磁盘文件 data/naming/namespace的id
配置数据是什么存储的?
    
    内置数据库derby,也可以切换成mysql

RestTemplate详解,b站搜下教程

rabbin扩展了resttemplate的ClientHttpRequestInterceptor,搞个了实现类LoadBalancerInterceptor
此处rabbin做的功能就是1.拉取所有的服务,2.把服务名换成真正的地址(ip加端口)
