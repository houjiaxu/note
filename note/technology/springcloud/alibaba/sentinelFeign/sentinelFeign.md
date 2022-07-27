[sentinel整合Feign的实现](https://www.cnblogs.com/javastack/p/10129409.html)

Sentinel 想要整合 Feign，可以参考 Hystrix 的实现：

    1.❌ 实现 Targeter 接口 SentinelTargeter。 很不幸，Targeter 这个接口属于包级别的接口，在外部包中无法使用，这个 Targeter 无法使用。没关系，
        我们可以沿用默认的HystrixTargeter(实际上会用 DefaultTargeter，下文 Note 有解释)
    
    2.✅ FeignClientFactoryBean 内部构造 Targeter、feign.Feign.Builder 的时候，都会从 FeignContext 中获取。所以我们沿用默认的 DefaultTargeter
        的时候，内部使用的 feign.Feign.Builder 可控，而且这个 Builder 不是包级别的类，可在外部使用创建 SentinelFeign.Builder 继承 feign.Feign.Builder ，
        用来构造 Feign
        SentinelFeign.Builder 内部需要获取 FeignClientFactoryBean 中的属性进行处理，比如获取 fallback, name, fallbackFactory。很不幸，
        FeignClientFactoryBean 这个类也是包级别的类。没关系，我们知道它存在在 ApplicationContext 中的 beanName， 拿到 bean 之后根据反射
        获取属性就行(该过程在初始化的时候进行，不会在调用的时候进行，所以不会影响性能)
    
        SentinelFeign.Builder 调用 build 方法构造 Feign 的过程中，我们不需要实现一个新的 Feign，跟 hystrix 一样沿用 ReflectiveFeign 即可，
        在沿用的过程中调用父类 feign.Feign.Builder 的一些方法进行改造即可，比如 invocationHandlerFactory 方法设置 InvocationHandlerFactory ，contract 的调用
    3.✅ 跟 hystrix 一样实现自定义的 InvocationHandler 接口 SentinelInvocationHandler 用来处理方法的调用
    
    4.✅ SentinelInvocationHandler 内部使用 Sentinel 进行保护，这个时候涉及到资源名的获取。SentinelInvocationHandler 内部的 feign.Target 能获取服务名信息，
        feign.InvocationHandlerFactory.MethodHandler 的实现类 feign.SynchronousMethodHandler 能拿到对应的请求路径信息。很不幸，feign.SynchronousMethodHandler
        这个类也是包级别的类。没关系，我们可以自定义一个 feign.Contract 的实现类 SentinelContractHolder 在处理 MethodMetadata 的过程把这些 metadata 保存下来(feign.Contract 
        这个接口在 Builder 构造 Feign 的过程中会对方法进行解析并验证)。
        在 SentinelFeign.Builder 中调用 contract 进行设置，SentinelContractHolder 内部保存一个 Contract 使用委托方式不影响原先的 Contract 过程 
    
    Note: spring-cloud-starter-openfeign 依赖内部包含了 feign-hystrix。所以是说默认使用 HystrixTargeter 这个 Targeter ，进入 HystrixTargeter 的 target 方法内部一看，发现有段逻辑这么写的：
        @Override
        public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context,Target.HardCodedTarget<T> target) {
            if (!(feign instanceof feign.hystrix.HystrixFeign.Builder)) {
                // 如果 Builder 不是 feign.hystrix.HystrixFeign.Builder，使用这个 Builder 进行处理
                // 我们默认构造了 SentinelFeign.Builder 这个 Builder，默认使用 feign-hystrix 依赖也没有什么问题
                return feign.target(target);
            }
            feign.hystrix.HystrixFeign.Builder builder = (feign.hystrix.HystrixFeign.Builder) feign;
            ...
        }
    在 SentinelInvocationHandler 内部我们对资源名的处理策略是: http方法:protocol://服务名/请求路径跟参数,比如这个 TestService:
        @FeignClient(name = "test-service")
        public interface TestService {
            //对应的资源名GET:http://test-service/echo/{str}
            @RequestMapping(value = "/echo/{str}", method = RequestMethod.GET)
            String echo(@PathVariable("str") String str);
            //对应的资源名：GET:http://test-service/divide
            @RequestMapping(value = "/divide", method = RequestMethod.GET)
            String divide(@RequestParam("a") Integer a, @RequestParam("b") Integer b);
        }


总结:

    1.Feign 的内部很多类都是 package 级别的，外部 package 无法引用某些类，这个时候只能想办法绕过去，比如使用反射
    2.目前这种实现有风险，万一哪天 starter 内部使用的 Feign 相关类变成了 package 级别，那么会改造代码。所以把 Sentinel 的实现放到 Feign 里并给 Feign 官方提 pr 可能更加合适
    3.Feign的处理流程还是比较清晰的，只要能够理解其设计原理，我们就能容易地整合进去
































