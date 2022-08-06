

对于Hystrix的扩展:强制打印降级前触发的异常日志(降级方法成功执行的情况下).
HystrixCommandExecutionHook:提供了对HystrixCommand及HystrixObservableCommand生命周期的钩子方法，可以自定义实现，做一些额外的处理，比如日志打印、覆盖response、更改线程状态等等。

对Eureka的扩展:目前某个服务[注册/取消注册]到eurekaServer时，依赖方需要等30秒至1分钟才能知晓,让eureka客户端使用sse技术连接到eurekaServer上，当eurekaServer检测到服务器注册、取消注册等事件时，通过sse主动通知客户端，随后客户端刷新服务器列表。

对Ribbon的扩展: 使用自定义的ping组件，每隔3秒访问服务器/monitor/check接口，如果不通或者返回不是OK，则标记为dead状态，去除对该服务器的流量（官方的只判断了从eureka取下来的状态是否UP，存在很大延迟.）

对httpclient的扩展: 加了hystrix限流熔断功能,当某个host下的http调用错误率或超时超过hystrix阈值时，对改host的调用进行熔断

对mybatis的扩展: 解决了springboot打成jar包后，运行时，mybatis会扫描typeAliasesPackage配置下的类，由于这个类在springboot jar内部的嵌入jar中，导致找不到该类。








