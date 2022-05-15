##cglib和jdk动态代理的区别

cglib代理中的类中的方法中调用自身类的方法仍然会被代理,而jdk不会.

##AOP的三大步

1.解析切面

2.创建动态代理 todo 在bean初始化之后的一个后置处理器里进行创建的.

3.调用, 在jdkDynamicAopProxy的invoke方法里进行调用的.

##AOP切面的解析
1.解析的大致逻辑

在第一次调用bean的后置处理器的时候进行解析的,会拿到所有的bean定义,判断是否被@Aspectj标记了,是的话会将@before/@after标注的方法解析成一个个advisor

advisor由2部分组成: advise(织入的代码)  poincut(切点)

BeanNameAutoProxyCreator这个类可以根据beannames和interceptornames创建代理

创建bean动态代理的地方: bean初始化之后的bean的后置处理器里创建动态代理, 会拿到所有的advisor, 循环(责任链式的调用,当前切点动态创建完,把创建完的bean传入下一个advisor再进行代理), 根据pointcut的matchs方法对bean进行匹配, 匹配成功,则创建动态代理, 最终的对象可能是被代理了很多次


2.怎么找切面是在哪里解析的?

spring通常整合扩展点的地方都会搞个@Enable**, 通常的解析就是从这个注解里面找
aop的解析就从@EnableAspectJAutoProxy里找,这个注解上又引入了@Import(AspectJAutoProxyRegistrar.class)
AspectJAutoProxyRegistrar实现了ImportBeanDefinitionRegistrar,重写了registerBeanDefinitions接口,此接口是可以向容器中注册bean定义的.
该方法的实现中调用了AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);
在这个方法中注册了AnnotationAwareAspectJAutoProxyCreator.class的beanDefinition, 这个类实现了BeanPostProcessor.

因为是在第一次调用bean后置处理器的地方进行解析,所以该类实现了InstantiationAwareBeanPostProcessor,

因为在解决循环引用的可能调用动态代理,所以该类实现了SmartInstantiationAwareBeanPostProcessor,

因为在bean初始化之后可能调用动态代理,所以该类实现了BeanPostProcessor,
![Alt](img/1652275823044_123.png)
是在一个shouldSkip方法里进行解析的, 注意看子类重写.


3.真正的解析切面

    ／／注册 AnnotationAwareAspectJAutoProxyCreator
    AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary注册或者升级 AnnotationAwareAspectJAutoProxyCeator
        
        useClassProxyingIfNecessary 处理 oxy-target-class 以及 expose-proxy 属性
        
        registerComponentIfNecessary 注册组件并通知,便于监听器做进一步处理
        
    //对于注解中子类的处理
	extendBeanDefinition(element, parserContext);

上面注册了 AnnotationAwareAspectJAutoProxyCreator,那么这个类干了啥?
    
