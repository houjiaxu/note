[Ribbon核心组件以及运行原理源码剖析](https://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484202&idx=1&sn=a89f4c393ca2d09b263aab79e4ebfd3d&chksm=cfd950e2f8aed9f4e6f49d93b8120d93382899b7d5eccab06b26c76042abf89a98653410b1d1&scene=21#wechat_redirect)

1.Nacos整合Ribbon如何实现微服务调用
2.Nacos整合Feign如何实现微服务调用
3.Ribbon调用原理分析
4.@LoadBalanced注解原理分析
5.Ribbon负载均衡策略配置
6.Ribbon自定义负载均衡策略

Feign里面封装了Ribbon,Feign对springmvc进行了支持,也就是可以直接对@RequestMapping里的路径进行调用
    
    @FeignClient(value="mall-order",path="/order")//value是服务名,path是前缀
    public interface OrderFeignService{
        @RequestMapping("/findOrderByUserId/{userid}")//这个方法可以直接从controller里复制过来
        public Result findOrderByUserId(@PathVariable("userid")Integer userid);
    }
    //然后就可以直接注入OrderFeignService,调用findOrderByUserId方法了

源码实现是在LoadBalancerAutoConfiguration类中

@Qualifier,一个是改变名称, 另一个是限定注入

    比如自定义个注解@MyQualifier 
    @target @retention @Documented @Inherited
    @Qualifier
    public @interface MyQualifier{}

    @Bean
    @MyQualifier
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @AutoWired
    @MyQualifier  //这里是注入了所有带有@MyQualifier注解的RestTemplate,如果没有带@MyQualifier注解,是不会被注入到这里面的
    private List<RestTemplate> restTemplates;



spring扩展点之SmartInitializingSingleton
    
    SmartInitializingSingleton看字面意思是单例bean初始化,里面有个afterSingletonsInstantiated方法需要重写,可以在这里进行一系列的操作,但是这个玩意具体在spring的哪一步调用的还得考究
    todo 看SmartInitializingSingleton这个是在spring的哪一步被调用的


扩展点:
    
    要区分springcloud的扩展点和ribbon的扩展点, 如果对ribbon的扩展点进行扩展, 那么就要依赖ribbon

异构服务(边车模式sidecar)



























