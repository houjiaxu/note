角色: 客户端,服务端.
![角色](img/1281657767188_.pic.jpg)

![架构](img/1291657767207_.pic.jpg)

Eureka核心功能点
    
    服务注册(register):存储在一个双层的Map中
    服务续约(renew)：心跳默认30秒
    服务同步(replicate): Eureka Server之间会互相进行服务同步,就是服务端集群
    获取服务(get registry): 消费者启动的时候，发送一个REST请求给Eureka Server，获取服务列表，并且缓存在Eureka Client本地，默认缓存30秒,Eureka Server也会维护一份只读的服务清单缓存，该缓存每隔30秒更新一次。
    服务调用:Eureka有Region和Zone的概念，一个Region可以包含多个Zone，在进行服务调用时，优先访问处于同一个Zone中的服务提供者。
    服务下线(cancel):当Eureka Client需要关闭或重启时，会提前先发送REST请求告诉Eureka Server自己要下线了，Eureka Server在收到请求后，就会把该服务状态置为下线（DOWN），并把该下线事件传播出去。
    服务剔除(evict)：Eureka Server在启动的时候会创建一个定时任务，每隔一段60秒，从当前服务清单中把超时没有续约的服务剔除,默认90秒。
    自我保护:短时间内，统计续约失败的比例，如果达到一定阈值，则会触发自我保护的机制，在该机制下，Eureka Server不会剔除任何的微服务，等到正常后，再退出自我保护机制。自我保护开关(eureka.server.enableself-preservation: false)
    
Server端源码分析

    @Configuration
    @Import(EurekaServerInitializerConfiguration.class)
    @ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)
    @EnableConfigurationProperties({ EurekaDashboardProperties.class, InstanceRegistryProperties.class })
    @PropertySource("classpath:/eureka/server.properties")
    public class EurekaServerAutoConfiguration extends WebMvcConfigurerAdapter {
        @Bean// 加载EurekaController, spring‐cloud 提供了一些额外的接口，用来获取eurekaServer的信息
        @ConditionalOnProperty(prefix = "eureka.dashboard", name = "enabled", matchIfMissing = true)
        public EurekaController eurekaController() {
            return new EurekaController(this.applicationInfoManager);
        }
    
        @Bean//初始化集群注册表
        public PeerAwareInstanceRegistry peerAwareInstanceRegistry(ServerCodecs serverCodecs) {
            this.eurekaClient.getApplications(); // force initialization
            return new InstanceRegistry(this.eurekaServerConfig, this.eurekaClientConfig, serverCodecs, this.eurekaClient,
                this.instanceRegistryProperties.getExpectedNumberOfRenewsPerMin(),
                this.instanceRegistryProperties.getDefaultOpenForTrafficCount());
        }
        
        @Bean// 配置服务节点信息，这里的作用主要是为了配置Eureka的peer节点，也就是说当有收到有节点注册上来的时候，需要通知给那些服务节点，（互为一个集群）
        @ConditionalOnMissingBean
        public PeerEurekaNodes peerEurekaNodes(PeerAwareInstanceRegistry registry, ServerCodecs serverCodecs) {
            return new PeerEurekaNodes(registry, this.eurekaServerConfig, this.eurekaClientConfig, serverCodecs, this.applicationInfoManager);
        }
        
        @Bean// EurekaServer的上下文
        public EurekaServerContext eurekaServerContext(ServerCodecs serverCodecs, PeerAwareInstanceRegistry registry, PeerEurekaNodes peerEurekaNodes) {
            return new DefaultEurekaServerContext(this.eurekaServerConfig, serverCodecs, registry, peerEurekaNodes, this.applicationInfoManager);
        }
        
        @Bean//该类是spring‐cloud和原生eureka的胶水代码，用来启动EurekaServer 后面该类会在EurekaServerInitializerConfiguration被调用，进行eureka启动
        public EurekaServerBootstrap eurekaServerBootstrap(PeerAwareInstanceRegistry registry, EurekaServerContext serverContext) {
            return new EurekaServerBootstrap(this.applicationInfoManager, this.eurekaClientConfig, this.eurekaServerConfig, registry, serverContext);
        }
        
        @Bean// 配置拦截器，ServletContainer里面实现了jersey框架，通过他来实现eurekaServer对外的restFull接口
        public FilterRegistrationBean jerseyFilterRegistration( javax.ws.rs.core.Application eurekaJerseyApp) {
            FilterRegistrationBean bean = new FilterRegistrationBean();
            bean.setFilter(new ServletContainer(eurekaJerseyApp));
            bean.setOrder(Ordered.LOWEST_PRECEDENCE);
            bean.setUrlPatterns( Collections.singletonList(EurekaConstants.DEFAULT_PREFIX + "/*"));
            return bean;
        }
    }
    
    @Import(EurekaServerInitializerConfiguration.class)





















