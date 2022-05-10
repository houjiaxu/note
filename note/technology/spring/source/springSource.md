#spring官方文档

https://docs.spring.io/spring-framework/docs/current/reference/html/

https://spring.io/projects

#源码笔记

https://www.yuque.com/books/share/5f19528d-d89b-4b69-a7bd-9f436a2dd734/bs9d13

#spring源码
##bean的创建过程
new AnnotationConfigApplicationContext(Config.class)

1.AnnotationConfigApplicationContext(Class<?>... annotatedClasses)
    
    this(); //调用构造函数, 创建了bdReader,注册了一些内置的后置处理器
        new AnnotatedBeanDefinitionReader(this);//创建一个读取注解的Bean定义读取器,完成了spring内部BeanDefinition的注册（主要是后置处理器）
            AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);//注册一些内置的后置处理器
                注册了实现Order接口的排序器 AnnotationAwareOrderComparator
                重置了@AutoWired的候选的解析器 ContextAnnotationAutowireCandidateResolver
                注册后置处理器
                    ConfigurationClassPostProcessor(配置解析)  internalConfigurationAnnotationProcessor  父类:BeanFactoryPostProcessor
                    AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                    RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                    CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
                    PersistenceAnnotationBeanPostProcessor(处理jpa注解)  
                    EventListenerMethodProcessor(处理监听方法的注解@EventListener)  父类:SmartInitializingSingleton, ApplicationContextAware
                    DefaultEventListenerFactory(注册事件监听器工厂)  父类:EventListenerFactory
        new ClassPathBeanDefinitionScanner(this);//创建BeanDefinition扫描器, 可以用来扫描包或者类，继而转换为bd
            registerDefaultFilters();//注册要扫描的类
                将Component.class添加至includeFilters
            setEnvironment(environment);//设置环境对象
            setResourceLoader(resourceLoader);//设置资源加载器
    register(annotatedClasses);//注册我们的配置类的definition
        AnnotatedBeanDefinitionReader#doRegisterBean
            shouldSkip//判断@Condition注解,是否需要注册,类上有@Configuration注解的不注册,但是要解析里面的@bean注解的类
            注册了一个Supplier
            AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);//解析通用注解:Lazy，Primary，DependsOn，Role，Description
            调用BeanDefinitionCustomizer#customize方法,这个是一个扩展点,自定义beandefinition的回调,扫描配置类的时候这个参数为空
            包装成BeanDefinitionHolder,判断是否需要创建代理
            BeanDefinitionReaderUtils.registerBeanDefinition//注册beanDefinition
    refresh();//IOC容器刷新接口,注册所有其他bean的definition
        prepareRefresh();//1:准备刷新上下文环境
            initPropertySources();//供子类实现的接口,假设子类重写在该方法中设置了一个环境变量的值为A,启动的时候，我的环境变量中没有该值就会启动抛出异常
            earlyApplicationListeners//创建一个早期事件监听器对象
            earlyApplicationEvents//创建一个容器用于保存早期待发布的事件集合
                什么是早期事件? 就是我们的事件监听器还没有注册到多播器上的时候都称为早期事件
		        早期事件不需要手动publishEvent发布， 在registerListeners中会自动发布， 发布完早期事件就不存在了。
        obtainFreshBeanFactory();//2:获取告诉子类初始化Bean工厂  不同工厂不同实现
            refreshBeanFactory();//xml加载spring会在这里加载beanDefinition, javaconfig只是刷新了beanFactory
        prepareBeanFactory(beanFactory);//3:对bean工厂进行填充属性
            设置类加载器
            设置SPEL表达式解析器对象StandardBeanExpressionResolver
            设置propertityEditor 属性资源编辑器对象(用于后面的给bean对象赋值使用)
            注册了一个BeanPostProcessor类型是ApplicationContextAwareProcessor用来处理ApplicationContextAware接口的回调方法
            设置忽略接口,主要是几个Aware接口,在populateBean时,这些接口有setXXX方法，如果不特殊处理将会自动注入容器中的bean,比如接口ApplicationContextAware
            注册可解析的依赖,当注册了依赖解析,再对bean的属性注入的时候，一旦检测到属性为ApplicationContext类型便会将applicationContext的实例注入进去。
                主要有BeanFactory  ResourceLoader  ApplicationEventPublisher   ApplicationContext
                    示例: @Autowired  ApplicationContext  applicationContext
            注册了一个事件监听器探测器后置处理器  ApplicationListenerDetector功能: 当bean为ApplicationListener时,将其添加到上下文的时间监听器列表里去
            注册后置处理器 LoadTimeWeaverAwareProcessor(加载时织入) 用来处理aspectj的: https://www.cnblogs.com/takumicx/p/10150344.html
            注册3个bean: environment(环境), systemProperties(环境系统属性), systemEnvironment(系统环境)
        postProcessBeanFactory(beanFactory);// 4:留个子类去实现该接口
            注册BeanPostProcessor,类型为ServletContextAwareProcessor
            WebApplicationContextUtils.registerWebApplicationScopes //注册web应用领域的范围和一些可解析依赖, 例如ServletRequest,HttpSession等,范围"request", "session", "globalSession", "application"
            WebApplicationContextUtils.registerEnvironmentBeans //注册环境bean,例如:servletContext,servletConfig,contextParameters,contextAttributes等
        //5.调用我们的bean工厂的后置处理器.里面会调用ConfigurationClassPostProcessor的后置处理,
        //1. 会在此将所有的class扫描成beanDefinition并注册  2.bean工厂的后置处理器调用
        invokeBeanFactoryPostProcessors(beanFactory);
            PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors
                处理BeanDefinitionRegistryPostProcessor
                    有注册能力的:
                        调用BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry,包括系统里的和自定义的以及框架扩展的
                            1.调用之前代码注册到beanfactory里的beanFactoryPostProcessors并且又属于BeanDefinitionRegistryPostProcessor的后置处理器
                                无
                            2.获取容器里所有的BeanDefinitionRegistryPostProcessor,进行调用内置实现PriorityOrdered接口的类
                                ConfigurationClassPostProcessor
                            3.调用实现了Order接口的BeanDefinitionRegistryPostProcessor
                                无
                            4.调用没有实现任何优先级接口的BeanDefinitionRegistryPostProcessor
                                无
                        调用BeanFactoryPostProcessor的postProcessBeanFactory方法
                            invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
                                ConfigurationClassPostProcessor
                            invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);//无
                    没有注册能力的
                        直接调用BeanFactoryPostProcessor.postProcessBeanFactory方法
                            无
                处理BeanFactoryPostProcessor#postProcessBeanFactory
                    1.过滤上一步已经处理过的
                    2.调用BeanFactoryPostProcessor类型实现了priorityOrdered接口的
                    3.调用BeanFactoryPostProcessor类型实现了Ordered接口的
                    4.调用BeanFactoryPostProcessor没有实现任何优先级接口的
                    5.自定义的,例如:MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor
            注册后置处理器 LoadTimeWeaverAwareProcessor(加载时织入) 用来处理aspectj的
        registerBeanPostProcessors(beanFactory);// 6.实例化并注册bean的后置处理器, 这一步走完除了自定义的之外, 也就autowire,require,common这3个内置的处理器
            1.把实现了priorityOrdered接口的后置处理器注册到容器中(在第一步AnnotatedBeanDefinitionReader的时候注册的,之前已经实例化过了,此处无需再次实例化)
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
            2.把实现了Order接口的后置处理器注册到容器中
                无
            3.把普通的没有实现任何排序接口的后置处理器注册到容器中
                自定义的后置处理器,例如:MyBeanPostProcessor
            4.把内部(实现了MergedBeanDefinitionPostProcessor接口的)后置处理器注册到容器中,Autowired注解正是通过此后置处理器实现诸如类型的预解析。
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
            5.注册ApplicationListenerDetector应用监听器探测器的后置处理器
                new ApplicationListenerDetector(applicationContext);
        initMessageSource();// 初始化国际化资源处理器.
            设置并注册父类消息源
        initApplicationEventMulticaster();// 创建事件多播器
            注册一个SimpleApplicationEventMulticaster对象,设置到上下文
        onRefresh();// 这个方法同样也是留个子类实现的springboot也是从这个方法进行启动tomcat的.
            initThemeSource//初始化主题资源
        registerListeners();//把我们的事件监听器注册到多播器上
            将所有的监听器ApplicationListener添加到多播器上ApplicationEventMulticaster, 然后通过多播器散播早期事件
        finishBeanFactoryInitialization(beanFactory);// 实例化我们剩余的单实例bean.
            创建类型转化器
            添加string值解析器(想起mvc中的ArgumentResolver了),增加一个嵌入式的StringValueResolver,比如说注解的属性.可以参考SpringMVC中的ArgumentResolver.
            示例化所有的LoadTimeWeaverAware,后面用来处理aspectj
            冻结所有的bean定义,说明注册的bean定义将不可被修改或任何进一步的处理
            beanFactory.preInstantiateSingletons();//实例化剩余的单实例bean,确保实例化了所有非lazy-init单例
                调用beanFactory.getBean(beanName)方法进行实例化
                    最终调用AbstractBeanFactory#doGetBean创建对象,详细过程见下方
                找出单例bean,调用SmartInitializingSingleton.afterSingletonsInstantiated();
        finishRefresh();// 最后容器刷新 发布刷新事件(Spring cloud也是从这里启动的)
            clearResourceCaches();//清除上下文级别的资源缓存
            initLifecycleProcessor//注册lifecycleProcessor声明周期处理器,作用：当ApplicationContext启动或停止时，它会通过LifecycleProcessor来与所有声明的bean的周期做状态更新
            getLifecycleProcessor().onRefresh();//为实现了SmartLifecycle并且isAutoStartup 自动启动的LifecycleGroup调用start()方法,比如AbstractApplicationContext#start()
            publishEvent(new ContextRefreshedEvent(this));//调用ApplicationEventMulticaster发布了容器启动完毕事件,如果是父容器，也会向父容器里广播一份
            LiveBeansView.registerApplicationContext(this);//注册当前spring容器到LiveBeansView,提供servlet(LiveBeansViewServlet)在线查看所有的bean json 、 为了支持Spring Tool Suite的智能提示
        
------------------------------------------------------------------------------------------------------
###AbstractBeanFactory#doGetBean
    getSingleton(beanName);//尝试去缓存中获取对象
        先去一级缓存中取
        一级缓存没取到 && 正在被创建  表明是循环依赖
        则从二级缓存中获取对象
        二级缓存没获取到,则从三级缓存中获取,
        从三级缓存中取出来的是ObjectFactory对象,调用该对象的getObject得到早期对象(裸对象/被代理后的对象)
        然后将早期对象放入二级缓存,再将ObjectFactory从三级缓存当中移除.
    获取到后调用getObjectForBeanInstance,再获取真正的对象,
        如果获取到的是普通的单例bean,则会直接返回。
        如果是FactoryBean类型的，则需调用 getObject 工厂方法获取真正的bean实例,
        如果用户想获取FactoryBean本身，也是直接返回
    没获取到则判断是否有父工厂, 
        有则调用父工厂的doGetBean或getBean方法,一般只有spring和springmvc的时候才有父子工厂
        没有父工厂则先调用getBean方法实例化依赖的bean,
            如果是单例bean,调用getSingleton(String beanName, ObjectFactory<?> singletonFactory)方法,并传入一个singletonFactory的实现
                从一级缓存中取数据,没取到则调用singletonFactory.getObject()进行创建
                    实现内部是调用createBean方法再调用createBean创建这个bean, createBean();//创建bean,详细看下方
                然后放入一级缓存中
    getTypeConverter().convertIfNecessary//检查所需类型是否与实际 bean 实例的类型匹配, 不匹配则进行转换
    
    说明:一级缓存存放的是完整的bean,二级缓存存放的是早期对象,三级缓存存放的是ObjectFactory对象
    二级缓存解决的是完整bean和早期bean的分离,如果只有一级缓存,当多个线程时,一个线程刚创建完早期对象,就有另一个线程在缓存中取,那么取到的则是早期对象, 是不完整的bean
    三级缓存解决的是解耦问题(也有说是aop的),如果只有二级缓存,也是可以解决代理对象的,就是要在二级缓存中取不到对象时,直接调用后置处理器,来创建一个对象,而spring是在bean的后置处理器被调用时才创建的.
    如果没有代理/aop,是没有走到三级缓存的
    
spring是怎么避免读取到不完整的bean的? 锁住一级缓存后面的创建过程.也就是说在一级缓存取不到的话会锁住后面的创建过程, 直到这个bean创建完毕
------------------------------------------------------------------------------------------------------
###createBean();//创建bean
    根据beanName解析出Class,设置到RootBeanDefinition中
    验证和准备覆盖方法(仅在XML方式中lookup-method 和 replace-method
    resolveBeforeInstantiation() 解析aop信息并缓存起来,事务并不会在此处被解析
        获取当前bean的class对象,然后判断是否有InstantiationAwareBeanPostProcessor(如果有自定义后置处理器实现这个接口,则在此处会调用)(第1次调用),有则调用以下2个方法:
            applyBeanPostProcessorsBeforeInstantiation,第一次调用bean后置处理器的postProcessBeforeInstantiation有以下3个
                内置的(只有3个,Autowired,Required,Common)
                ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
                很重要: 我们AOP @EnableAspectJAutoProxy 为我们容器中导入了 AnnotationAwareAspectJAutoProxyCreator,我们事务注解@EnableTransactionManagement 为我们的容器导入了 InfrastructureAdvisorAutoProxyCreator,都是实现了我们的 BeanPostProcessor接口,InstantiationAwareBeanPostProcessor,进行后置处理解析切面
            bean不等于null 则applyBeanPostProcessorsAfterInitialization, 此处无调用
            返回bean,此时bean应该是null, 还要走到下一步doCreateBean
    doCreateBean,该步骤是我们真正的创建我们的bean的实例对象的过程
        createBeanInstance(beanName, mbd, args)//使用合适的实例化策略来创建新的实例：工厂方法、构造函数自动注入、简单初始化 该方法很复杂也很重要
            resolveBeanClass(mbd, beanName)////从bean定义中解析出当前bean的class对象
            //todo 下面的几种创建方式评估下是否要详细探究
            obtainFromSupplier//该方法是spring5.0 新增加的,如果存在 Supplier 回调，则使用给定的回调方法初始化策略,从而创建bean实例
            instantiateUsingFactoryMethod();//工厂方法,我们通过配置类来进行配置的话,采用的就是工厂方法创建,在配置类里的@Bean就是通过这里创建
            若被解析过:
                autowireConstructor//通过有参的构造函数进行反射调用
                instantiateBean//调用无参数的构造函数进行创建对象
            没被解析过:
                determineConstructorsFromBeanPostProcessors//通过bean的后置处理器进行选举出合适的构造函数对象
                    调用SmartInstantiationAwareBeanPostProcessor.determineCandidateConstructors (第2次调用)
                        ConfigurationClassPostProcessor的内部类RequiredAnnotationBeanPostProcessor
                        AutowiredAnnotationBeanPostProcessor
                        RequiredAnnotationBeanPostProcessor
                autowireConstructor//通过有参的构造函数进行反射调用
                instantiateBean//调用无参数的构造函数进行创建对象
        applyMergedBeanDefinitionPostProcessors()//进行后置处理 @AutoWired @Value的注解的预解析
            调用MergedBeanDefinitionPostProcessor.postProcessMergedBeanDefinition (第3次调用)
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                ApplicationListenerDetector
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));//缓存单例到三级缓存中,3个缓存都存了
            getEarlyBeanReference()//把我们的早期对象包装成一个singletonFactory对象 该对象提供了一个getObject方法,该方法内部调用getEarlyBeanReference方法,这是三级缓存
                SmartInstantiationAwareBeanPostProcessor.getEarlyBeanReference(第4次调用)
                    ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                    AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                    RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                    如果有代理/aop的话会调用这个类AbstractAutoProxyCreator,这里会创建代理
        populateBean//属性赋值 给我们的属性进行赋值(调用set方法进行赋值)
            InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation//在属性被填充前，后置处理器一个修改bean 状态的机会。(第5次调用)
                ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
            判断bean的属性注入模型ByName或ByType,然后进行注入,其实就是注册bean之间的依赖关系到map中,Map<bean,Set<被依赖bean>>, 被依赖Map<被依赖bean,Set<依赖bean>> 
            InstantiationAwareBeanPostProcessor.postProcessPropertyValues //后置处理，用于在Spring填充属性到之前前，对属性的值进行相应的处理，比如可以修改某些属性的值。这时注入到 bean 中的值就不是配置文件中的内容了，而是经过后置处理器修改后的内容(第6次调用)
                ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                CommonAnnotationBeanPostProcessor(处理JSR规范)   internalCommonAnnotationProcessor
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
            applyPropertyValues//上面是将获取的属性封装在PropertyValues的实例对象pvs中，这一步应用到已经实例化的bean中。
        initializeBean//进行对象初始化操作(在这里可能生成代理对象)
            invokeAwareMethods(beanName, bean)//若我们的bean实现了XXXAware接口进行方法的回调
            applyBeanPostProcessorsBeforeInitialization//调用所有bean后置处理器的postProcessorsBeforeInitialization方法   @PostCust注解的方法 (第7次调用)
                ApplicationContextAwareProcessor
                ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                PostProcessorRegistrationDelegate内部类BeanPostProcessorChecker
                CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
                    在InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization里调用了@PostCust注解的方法
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                ApplicationListenerDetector
            invokeInitMethods//调用初始化方法 bean的init()方法 
                ((InitializingBean) bean).afterPropertiesSet();回调InitializingBean的afterPropertiesSet()方法
                invokeCustomInitMethod//调用我们自己的初始化方法
            applyBeanPostProcessorsAfterInitialization//调用所有的bean的后置处理器的postProcessAfterInitialization方法, aop生成代理对象就是在这里 (第8次调用)
                ApplicationContextAwareProcessor
                ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
                PostProcessorRegistrationDelegate内部类BeanPostProcessorChecker
                CommonAnnotationBeanPostProcessor
                AutowiredAnnotationBeanPostProcessor(处理@Autowired注解)    internalAutowiredAnnotationProcessor
                RequiredAnnotationBeanPostProcessor(处理@Required注解)     internalRequiredAnnotationProcessor
                ApplicationListenerDetector
        如果是早起暴露对象, 则去3级缓存的前2级取早期对象
        registerDisposableBeanIfNecessary//注册销毁的bean的销毁接口
            以beanName当做key,注册一个销毁bean放入map(disposableBeans)中,以便销毁时回调
        
 ##销毁bean
        可以参考AbstractApplicationContext.refresh().destroyBeans();
        销毁bean的时候AbstractAutowireCapableBeanFactory#destroyBean(Object existingBean)
            new DisposableBeanAdapter(existingBean, getBeanPostProcessors(), getAccessControlContext()).destroy();
                filterPostProcessors(postProcessors, bean);
                    DestructionAwareBeanPostProcessor.requiresDestruction  (第9次调用)
------------------------------------------------------------------------------------------------------
##Aware的主要会被spring处理的实现类
    Aware的主要会被spring处理的实现类都在ApplicationContextAwareProcessor#invokeAwareInterfaces的方法里
    EnvironmentAware
    EmbeddedValueResolverAware
    ResourceLoaderAware
    ApplicationEventPublisherAware
    MessageSourceAware
    ApplicationContextAware


-----------------------------------后置处理器---------------------------都需要断点看下----------------------------------------
##后置处理器的调用
    第1次调用好像啥也没处理,全部都是返回null: resolveBeforeInstantiation在初始化之前进行解析
    第2次调用: createBeanInstance创建bean实例的时候,选择对应的构造函数
        AutowiredAnnotationBeanPostProcessor.determineCandidateConstructors  选出对应的构造函数,并放入缓存
    第3次调用:applyMergedBeanDefinitionPostProcessors()//进行后置处理 @AutoWired @Value的注解的预解析
        CommonAnnotationBeanPostProcessor.postProcessMergedBeanDefinition  
            super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName);//扫描了class,将init和destroy方法合并到BeanDefinition里去
            findResourceMetadata(beanName, beanType, null);//处理了@Resource注解,合并到BeanDefinition里
        AutowiredAnnotationBeanPostProcessor.postProcessMergedBeanDefinition
            寻找bean中所有被@Autowired注释的属性，并将属性封装成InjectedElement类型,合并到BeanDefinition里
        ApplicationListenerDetector.postProcessMergedBeanDefinition
            保存到ApplicationListenerDetector类里的map里, 在bean的初始化操作的后置处理里面, 判断是ApplicationListener的话,会将其注册到全局监听器里
    第4次调用:这一步应该怎么做断点? 循环依赖的时候会调用到,动态代理aop  : addSingletonFactory获取早期bean的地址, 并放入缓存, 全都调到了InstantiationAwareBeanPostProcessorAdapter的方法,然后直接返回了,打断点也没走
        ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor.getEarlyBeanReference
        AutowiredAnnotationBeanPostProcessor.getEarlyBeanReference
        RequiredAnnotationBeanPostProcessor.getEarlyBeanReference
    第5次调用也没做什么处理,只是返回true
    第6次调用:调用 postProcessPropertyValues 方法,填充属性,将其应用到bean
        ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
            如果bean是EnhancedConfiguration类型的,则((EnhancedConfiguration) bean).setBeanFactory(beanFactory);EnhancedConfiguration类型是需要增强的,做代理的,所以要beanFactory
        CommonAnnotationBeanPostProcessor
            findResourceMetadata(beanName, bean.getClass(), pvs);//找到被@Resource修饰的属性或方法,前面已经解析过了,这里只是从缓存拿
            metadata.inject(bean, beanName, pvs);//然后给该属性或者方法进行注入
        AutowiredAnnotationBeanPostProcessor
            findAutowiringMetadata(beanName, bean.getClass(), pvs);//从缓存中拿到注解元数据， 缓存没有载解析一遍
            metadata.inject(bean, beanName, pvs);//注入
                element.inject(target, beanName, pvs);//循环注入，这里有可能是AutowiredFieldElement也可能AutowiredMethodElement，因此调用的inject是2个不同的方法
                    todo 这里的注入,要仔细看一看,注入里面还有个解析,这个解析也挺多的
                    AutowiredFieldElement.inject
        RequiredAnnotationBeanPostProcessor
            校验必备属性
    第7次调用: initializeBean 初始化bean的前置调用,调用 postProcessBeforeInitialization 方法
        ApplicationContextAwareProcessor
            调用了aware接口的set方法
        ConfigurationClassPostProcessor的内部类ImportAwareBeanPostProcessor
            处理了ImportAware的setImportMetadata方法
        CommonAnnotationBeanPostProcessor
            在InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization里调用了@PostCust注解的方法
    第8次调用 initializeBean 初始化bean的后置调用, postProcessAfterInitialization 方法
        PostProcessorRegistrationDelegate内部类BeanPostProcessorChecker
            做了个BeanPostProcessor的数量的校验
        ApplicationListenerDetector
            判断bean如果是ApplicationListener,会将其注册到全局监听器里

-----------------------------------第6次调用BeanPostProcessor中的Autowired-------------------------------------------------------------------
##Autowired的处理
    AutowiredAnnotationBeanPostProcessor.postProcessPropertyValues
        findAutowiringMetadata(beanName, bean.getClass(), pvs);//从缓存中拿到注解元数据， 缓存没有载解析一遍
        metadata.inject(bean, beanName, pvs);//注入
            element.inject(target, beanName, pvs);//循环注入，这里有可能是AutowiredFieldElement也可能AutowiredMethodElement，因此调用的inject是2个不同的方法
                todo 这里的注入,要仔细看一看
                AutowiredFieldElement.inject
                    beanFactory.resolveDependency //通过beanFactory获取属性对应的值，根据工厂中定义的bean解析指定的依赖项,根据类型查找依赖,支持 Optional、延迟注入、懒加载注入、正常注入。
                        1. createOptionalDependency(descriptor, requestingBeanName);//Optional类型处理: JDK8解决空指针异常的 
                            这里面也是调用了第4. doResolveDependency() 正常情况 真正的解析依赖
                        2. new DependencyObjectProvider(descriptor, requestingBeanName)//延迟依赖注入支持：依赖类型为ObjectFactory、ObjectProvider
                            这个类的有个getObject(), 里面会调用createOptionalDependency或者doResolveDependency
                        3. 延迟依赖注入支持：javaxInjectProviderClass 类注入的特殊处理
                            Jsr330ProviderFactory().createDependencyProvider
                        4. 延迟依赖注入支持：@Lazy  getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary
                            设置了TargetSource类, 这个类里有个getTarget()接口, 里面调用了doResolveDependency
                        5. doResolveDependency() 正常情况 真正的解析依赖
                            5.1 快速查找，根据名称查找。 descriptor.resolveShortcut;  AutowiredAnnotationBeanPostProcessor用到, 查找到直接返回了
                            5.2 注入指定值按照@Qualifier解析; QualifierAnnotationAutowireCandidateResolver解析@Value会用到
                            5.3 集合依赖，resolveMultipleBeans();  如 Array、List、Set、Map。内部查找依赖也是使用findAutowireCandidates
                            5.4 单个依赖查询 ，查找匹配到得个数
                                5.5.1 匹配数为0, 则判断是否为Require,是则抛异常, 否则返回null.
                                5.5.2 找到多个, 则按@Primary -> @Priority -> 方法名称或字段名称匹配.
                                5.5.3 找到1个, 则调用getBean(autowiredBeanName, type)获取实例并返回.
                    registerDependentBeans(beanName, autowiredBeanNames);//注册bean之间的依赖关系放入map, 实际并没有注册bean, bean的注册在上一步的doResolveDependency里已经注册并实例化了
                    field.set(bean, value);//注入属性当中
-----------------------------------小知识点-------------------------------------------------------------------
##小知识点
    findAutowiredAnnotation: this.autowiredAnnotationTypes
        @Autowired,@Value,@Inject
    LifecycleElement:表示方法上有注解,然后封装成此类
    LifecycleMetadata:bean带注解的,并且和init/destroy相关的方法封装成类, 说白了就是带@PostConstruct/@PreDestry的方法封装成的类
        LifecycleMetadata看表面意思就是和生命周期相关的元数据.
    InjectedElement: 注入相关的元素 包括@Resource,@Autowired
    InjectionMetadata: 注入相关的原数据
    EnhancedConfiguration:是一个标记接口,是让所有@configuration cglib子类实现
    一般会自定义哪些后置处理器? 是要根据类型吧, 每一个调用的地方的类型和方法都不一样.

##AOP切面的解析
切面的解析是在AspectJAutoProxyBeanDefinitionParser的parse函数中进行
    
    ／／注册 AnnotationAwareAspectJAutoProxyCreator
    AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary注册或者升级 AnnotationAwareAspectJAutoProxyCeator
        
        useClassProxyingIfNecessary 处理 oxy-target-class 以及 expose-proxy 属性
        
        registerComponentIfNecessary 注册组件并通知,便于监听器做进一步处理
        
    //对于注解中子类的处理
	extendBeanDefinition(element, parserContext);

上面注册了 AnnotationAwareAspectJAutoProxyCreator,那么这个类干了啥?
    



    







