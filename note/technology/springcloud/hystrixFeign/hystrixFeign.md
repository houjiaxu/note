hystrix整合Feign

HystrixFeign 的详细构建过程：普通的DefaultTargeter构建过程也是这个,只不过此处实现类是HystrixTargeter.

    @EnableFeignClients -> FeignClientsRegistrar 扫描 @Feign注解的类 -> FeignClientFactoryBean通过Targeter生产FeignClient 
    -> Targeter通过Feign.Builder构建Feign -> Feign.Builder

整合原理:

    FeignAutoConfiguration自动配置类,通过spring.factories加载
        //有HystrixFeign这个类时则注入Targeter为HystrixTargeter,没有时则注入DefaultTargeter
        //看起来似乎可以使用自定义的Targeter代替HystrixTargeter或DefaultTargeter，这样就可以自定义各种功能了。实际上不行，因为 Targeter 是 protected 访问级别的。
        @Configuration
        @ConditionalOnClass(name = "feign.hystrix.HystrixFeign")
        protected static class HystrixFeignTargeterConfiguration {
            @Bean
            @ConditionalOnMissingBean
            public Targeter feignTargeter() {
                return new HystrixTargeter();
            }
        }
        @Configuration
        @ConditionalOnMissingClass("feign.hystrix.HystrixFeign")
        protected static class DefaultFeignTargeterConfiguration {
            @Bean
            @ConditionalOnMissingBean
            public Targeter feignTargeter() {
                return new DefaultTargeter();
            }
        }

    FeignClientsConfiguration
        //重要：Feign 以及内部类 Feign.Builder 都是 public 访问级别，可以注入自定义的bean。
        @Bean
        @ConditionalOnMissingBean
        public Retryer feignRetryer() {
            return Retryer.NEVER_RETRY;
        }
        @Bean
        @Scope("prototype")
        @ConditionalOnMissingBean
        public Feign.Builder feignBuilder(Retryer retryer) {
            return Feign.builder().retryer(retryer);
        }
        @Configuration
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

HystrixTargeter解析

    class HystrixTargeter implements Targeter {
        @Override
        public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign,
                FeignContext context, Target.HardCodedTarget<T> target) {
            // 此处只处理HystrixFeign。若不是 HystrixFeign，则执行其对应的默认target方法。
            if (!(feign instanceof feign.hystrix.HystrixFeign.Builder)) {
                return feign.target(target);
            }
            //强制转换成HystrixFeign.Builder
            feign.hystrix.HystrixFeign.Builder builder = (feign.hystrix.HystrixFeign.Builder) feign;
            SetterFactory setterFactory = getOptional(factory.getName(), context,SetterFactory.class);
            if (setterFactory != null) {
                builder.setterFactory(setterFactory);
            }
            Class<?> fallback = factory.getFallback();
            if (fallback != void.class) {
                return targetWithFallback(factory.getName(), context, target, builder,
                        fallback);
            }
            Class<?> fallbackFactory = factory.getFallbackFactory();
            if (fallbackFactory != void.class) {
                return targetWithFallbackFactory(factory.getName(), context, target, builder,
                        fallbackFactory);
            }
    
            // 调用从Feign.Builder继承的方法。即使注入的 Targeter 是 HystrixTargeter,此处也可以执行自定义 Feign.Builder。
            return feign.target(target);
        }
        
        private <T> T targetWithFallbackFactory(String feignClientName, FeignContext context,
                Target.HardCodedTarget<T> target, HystrixFeign.Builder builder,Class<?> fallbackFactoryClass) {
            FallbackFactory<? extends T> fallbackFactory = (FallbackFactory<? extends T>) getFromContext(
                    "fallbackFactory", feignClientName, context, fallbackFactoryClass,
                    FallbackFactory.class);
            return builder.target(target, fallbackFactory);
        }
    
        private <T> T targetWithFallback(String feignClientName, FeignContext context,
                Target.HardCodedTarget<T> target, HystrixFeign.Builder builder,
                Class<?> fallback) {
            T fallbackInstance = getFromContext("fallback", feignClientName, context,
                    fallback, target.type());
            return builder.target(target, fallbackInstance);
        }
        
        //...
    }

Feign.Builder

    Feign.Builder#target(Target) 方法通常不会被 override,因为没必要,target方法 return build().newInstance(target);
    所以直接重写build()方法即可,Feign 以及内部类 Feign.Builder 都是 public ，可以重写并注入自定义的bean。

HystrixFeign 
    
    public final class HystrixFeign {
    public static final class Builder extends Feign.Builder {  
        @Override
        public Feign build() {
            return build(null);
        }
        // 提供一系列的target方法，支持各种配置：fallback、FallBackFactory等
        public <T> T target(Target<T> target, T fallback) {
          return build(fallback != null ? new FallbackFactory.Default<T>(fallback) : null).newInstance(target);
        }
    
        public <T> T target(Target<T> target, FallbackFactory<? extends T> fallbackFactory) {
          return build(fallbackFactory).newInstance(target);
        }
          
        
        public <T> T target(Class<T> apiType, String url, T fallback) {
          return target(new Target.HardCodedTarget<T>(apiType, url), fallback);
        }
    
        public <T> T target(Class<T> apiType, String url, FallbackFactory<? extends T> fallbackFactory) {
          return target(new Target.HardCodedTarget<T>(apiType, url), fallbackFactory);
        }
    
        /** Configures components needed for hystrix integration. */
        Feign build(final FallbackFactory<?> nullableFallbackFactory) {
          super.invocationHandlerFactory(new InvocationHandlerFactory() {
            @Override
            public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
              return new HystrixInvocationHandler(target, dispatch, setterFactory,
                  nullableFallbackFactory);
            }
          });
          super.contract(new HystrixDelegatingContract(contract));
          return super.build();
        }
    基本到了这一步，需要设置的东西，都可以配置了。
    虽然 build 方法中涉及到 InvocationHandler,但基本不需要改什么，而 InvocationHandler 竟然也是 package 访问级别，所以只好复制一个，使用自己的。
    HystrixDelegatingContract 是 public 级别，不需要修改的话，仍然用这个。

自定义扩展示例:示例参考 SentinelFeign,其中的 YiFeiXiInvocationHandler 和 YiFeiXiFeignFallbackFactory 是自定义的。

    @Override
    public Feign build() {
        super.invocationHandlerFactory(new InvocationHandlerFactory() {
            @Override
            public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
                //使用反射从FeignClientFactoryBean获取 fallback and fallbackFactory的properties, 因为FeignClientFactoryBean是package级别的类,我们没办法直接使用
                Object feignClientFactoryBean = Builder.this.applicationContext.getBean("&" + target.type().getName());
                Class fallback = (Class) getFieldValue(feignClientFactoryBean,"fallback");
                Class fallbackFactory = (Class) getFieldValue(feignClientFactoryBean,"fallbackFactory");
                String name = (String) getFieldValue(feignClientFactoryBean, "name");
    
                Object fallbackInstance;
                FallbackFactory fallbackFactoryInstance;
                // 以下逻辑在HystrixTargeter中有，但执行自定义的builder，不会执行到那段逻辑，因此此处加上。
                if (void.class != fallback) {
                    fallbackInstance = getFromContext(name, "fallback", fallback,target.type());
                    return new YiFeiXiInvocationHandler(target, dispatch, setterFactory,new FallbackFactory.Default(fallbackInstance));
                }
                if (void.class != fallbackFactory) {
                    fallbackFactoryInstance = (FallbackFactory) getFromContext(name,"fallbackFactory", fallbackFactory,FallbackFactory.class);
                    return new YiFeiXiInvocationHandler(target, dispatch, setterFactory,fallbackFactoryInstance);
                }
                // 若注解中没有使用fallback或fallbackFactory，则使用一个默认的FallbackFactory。
                return new YiFeiXiInvocationHandler(target, dispatch, setterFactory, new YiFeiXiFeignFallbackFactory<>(target));
            }
    
            private Object getFromContext(String name, String type,Class fallbackType, Class targetType) {
                Object fallbackInstance = feignContext.getInstance(name,fallbackType);
                if (fallbackInstance == null) {
                    throw new IllegalStateException(String.format(
                        "No %s instance of type %s found for feign client %s",
                        type, fallbackType, name));
                }
    
                if (!targetType.isAssignableFrom(fallbackType)) {
                    throw new IllegalStateException(String.format(
                        "Incompatible %s instance. Fallback/fallbackFactory of type %s is not assignable to %s for feign client %s",
                        type, fallbackType, targetType, name));
                }
                return fallbackInstance;
            }
        });
    
        super.contract(new HystrixDelegatingContract(contract));
        return super.build();
    }
    //需要自定义fallbackFactory，则实现 feign.hystrix.FallbackFactory类，需要自定义fallback，则实现 org.springframework.cglib.proxy.MethodInterceptor即可
总结:

    由于Feign构建过程所用到的 Targeter 是 package 访问级别的，不能使用自定义的
    Feign以及Feign.Builder是 publilc，给了我们扩展的空间。
    















