[OpenFeign之FeignClient动态代理生成原理](https://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484185&idx=1&sn=efb3a1f459be9970126269234ff813e7&chksm=cfd950d1f8aed9c7c9ec6bc8b00c376d9777aa6d6aa2b93ccf6a4b4376adbed8c4f3e1e3754b&scene=21#wechat_redirect)

[OpenFeign原来是这么基于Ribbon来实现负载均衡的](https://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484211&idx=1&sn=13b1cb0832bfae9a6d2369193700fd19&chksm=cfd950fbf8aed9ed473a0e170480770c311f1b637607332a0df15f32e2e9a446f8bc97f0b295&scene=21#wechat_redirect)

@EnableFeignClients作用源码剖析

    使用Feign，需要使用@EnableFeignClients，@EnableFeignClients的作用可以扫描指定包路径下的@FeignClient注解，也可以声明配置类；
    @EnableFeignClients注解其实就是整个feign的入口,该注解上有个@Import(FeignClientsRegistrar.class),FeignClientsRegistrar实现了ImportBeanDefinitionRegistrar
    查看实现方法registerBeanDefinitions
        registerDefaultConfiguration(metadata, registry);//注入一些配置，就是对EnableFeignClients注解属性的解析,实际是注入了一个FeignClientSpecification的bean定义
        registerFeignClients(metadata, registry);
            ClassPathScanningCandidateComponentProvider scanner = getScanner();//获取扫描器
            annotationTypeFilter = new AnnotationTypeFilter(FeignClient.class);//创建过滤器,用来过滤@FeignClient注解
            metadata.getAnnotationAttributes(EnableFeignClients.class.getName());//找到@EnableFeignClients标注的类所在报名作为扫描包
            scanner.findCandidateComponents(basePackage)//扫描包下的类,注册成bean定义
            registerClientConfiguration//每个Feign的客户端的配置类封装成一个FeignClientSpecification的BeanDefinition，注册到spring容器中。后面会有用.
            registerFeignClient(registry, annotationMetadata, attributes);
                重新构造了一个BeanDefinition，这个BeanDefinition的指定的class类型是FeignClientFactoryBean
    总结:这个类的主要作用是扫描指定（不指定就默认路径下的）所有加了@FeignClient注解的类，然后每个类都会生成一个BeanDefinition，随后遍历每个BeanDefinition，然后取出每个@FeignClient注解的属性，
        构造新的BeanDefinition，传入FeignClientFactoryBean的class，随后注入到spring容器中；同时有配置类的也会将配置类构件出一个bean class
        为FeignClientSpecification的BeanDefinition注入到spring容器中。
![](img/1241657695714_.pic.jpg)            
    
Feign客户端接口动态代理生成源码剖析

    FeignAutoConfiguration类分析
        //注入了上面提到的FeignClientSpecification,然后设置到FeignContext中,并将FeignContext注入到容器
        //一个Feign客户端的对应一个FeignClientSpecification
        @Autowired(required = false)
    	private List<FeignClientSpecification> configurations = new ArrayList<>();
        @Bean
        public FeignContext feignContext() {
            FeignContext context = new FeignContext();
            context.setConfigurations(this.configurations);
            return context;
        }
     
    FeignContext类分析   
        //FeignContext继承了NamedContextFactory，构造的时候，传入了FeignClientsConfiguration，这个玩意也很重要
        public class FeignContext extends NamedContextFactory<FeignClientSpecification> {
          public FeignContext() {
            super(FeignClientsConfiguration.class, "feign", "feign.client.name");
          }
        }
    
    NamedContextFactory类分析
        NamedContextFactory的作用是用来进行配置隔离的，ribbon和feign的配置隔离都依赖这个抽象类。
        每个Feign客户端都有可能有自己的配置，从@FeignClient注解的属性configuration可以看出，所以写了这个类，用来隔离每个客户端的配置，
            这就是为什么在构造FeignContext传入一堆FeignClientSpecification的原因，这里封装了每个客户端的配置类。
            其实所谓的配置隔离就是为每个客户端构建一个AnnotationConfigApplicationContext，然后基于这个ApplicationContext来解析配置类，这样就实现了配置隔离。
            
        Map<String, AnnotationConfigApplicationContext> contexts = new ConcurrentHashMap<>();//一个客户端一个对应的AnnotationConfigApplicationContext
        Map<String, C> configurations = new ConcurrentHashMap<>();//一个客户端一个配置类的封装，对应到Feign的就是FeignClientSpecification
        private ApplicationContext parent; //父类 ApplicationContext ，也就是springboot所使用的ApplicationContext
        private Class<?> defaultConfigType;// 这个是默认的额配置类

    FeignClientsConfiguration类分析
        这是一个默认的配置类，里面配置了很多bean，这些bean都是生成Feign客户端动态代理的需要的，列几个重要的。
        @Bean//主要作用是用来解析@FeignClient接口中每个方法使用的springmvc的注解的，这也就是为什么FeignClient可以识别springmvc注解的原因。
        @ConditionalOnMissingBean
        public Contract feignContract(ConversionService feignConversionService) {
            return new SpringMvcContract(this.parameterProcessors, feignConversionService);
        }
        
        @Bean//用来构建动态代理的类，通过这个类的target方法，就能生成Feign动态代理
        @Scope("prototype")
        @ConditionalOnMissingBean
        public Feign.Builder feignBuilder(Retryer retryer) {
            return Feign.builder().retryer(retryer);
        }
        
        //FeignClientsConfiguration的内部类，是用来整合hystrix的，@ConditionalOnProperty(name = "feign.hystrix.enabled")，
        //当在配置文件配置了feign.hystrix.enabled=true的时候，就开启了hystrix整合了Feign，然后调用Feign的接口就有了限流、降级的功能。
        //其实hystrix整合Feign很简单，就是在构造动态代理的时候加了点东西而已。其实不光是hystrix，spring cloud alibaba中的sentinel
        //在整合Feign的适合也是按照这个套路来的。
        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass({ HystrixCommand.class, HystrixFeign.class })
          protected static class HystrixFeignConfiguration {
            @Bean
            @Scope("prototype")
            @ConditionalOnMissingBean
            @ConditionalOnProperty(name = "feign.hystrix.enabled")
            public Feign.Builder feignHystrixBuilder() {
              return HystrixFeign.builder();
            }
        }
    
构建动态代理的过程源码剖析

    总结代理对象生成的过程:每个Feign客户端都有对应的一个spring容器，用来解析配置类，根据配置从容器获取到一个Feign.Builder，然后再从容器中获取每个组件，填充到Feign.Builder中，最后通过Feign.Builder的build方法来构造动态代理，构造的过程其实是属于feign包底下的。
    
    @EnableFeignClinets会扫描出每个加了@FeignClient注解的接口，然后生成对应的BeanDefinition，最后重新生成一个bean class为FeignClientFactoryBean的BeanDefinition，注册到spring容器。
    接下来就会根据BeanDefinition来生成feign客户端的代理对象了。通过FeignClientFactoryBean的getObject方法来获取到代理对象
    FeignClientFactoryBean#getObject
        FeignContext context = this.applicationContext.getBean(FeignContext.class);//FeignContext里面封装了每个Feign的配置，起到配置隔离的作用。
        Feign.Builder builder = feign(context);//获取到一个Feign.Builder，默认是在FeignClientsConfiguration中配置的
            Feign.Builder builder = get(context, Feign.Builder.class)
            configureFeign(context, builder);//这个是从配置文件中读取feign的配置,所以如果要替换什么东西,可以在@FeignClient的configuration配置里进行替换,扩展点
        this.name.startsWith("http");//判断你有没有指定url,在@FeignClient注解中指定的url属性，这个属性是主要是进行feign直连,就是直接访问ip+端口, 不通过注册中心
        loadBalance(builder, context,new HardCodedTarget<>(this.type, this.name, this.url));//HardCodedTarget里封装了Feign客户端接口的类型、服务名、还有刚构建的url(http://ServiceA)
            Client client = getOptional(context, Client.class);//从feign客户端对应的ioc容器中获取一个Client
                FeignClientsConfiguration里面没有配置Client这个bean，那是从哪来呢？ 答案是ribbon,此处先不写整合ribbon.
            Targeter targeter = get(context, Targeter.class);//Targeter是通过FeignAutoConfiguration来配置的，默认是DefaultTargeter，如果整合hystrix就需是HystrixTargeter。
            targeter.target(this, builder, context, target);//实际调用DefaultTargeter#target
                feign.target(target);//实际调用Feign.Builder#tartget
                    build().newInstance(target);
                        build方法将最开始填充到Feign.Builder给封装起来，构建了一个ReflectiveFeign，然后调用ReflectiveFeign的newInstance方法，传入Target<T> target，也就是前面传入的HardCodedTarget。
                        newInstance方法通过Target拿到接口的类型，然后获取到所有的方法，遍历每个方法，处理之后放入methodToHandler中，然后通过InvocationHandlerFactory的create方法，传入methodToHandler
                            和Target，获取到一个InvocationHandler，之后通过jdk的动态代理，生成一个代理对象，然后返回回去。InvocationHandler默认是ReflectiveFeign.FeignInvocationHandler
![](img/1251657704995_.pic.jpg)

多次触发ContextRefreshedEvent事件的坑

    不知道大家有么有遇到过这个坑，就是在spring cloud环境中，监听类似ContextRefreshedEvent这种事件的时候，这个事件会无缘无故地触发很多次，
        其实就是这个原因就在这，因为spring的事件是有传播机制的，每个客户端对应的容器都要进行refresh，refresh完就会发这个事件，然后这个事件
        就会传给parent容器，也就是springboot启动的容器，就会再次触发，所以如果客户端很多，那么就会触发很多次。解决办法就是进行唯一性校验，
        只能启动一次就行了。

springcloud预留的供实现的扩展接口有哪些?

    比如注册中心的等

























