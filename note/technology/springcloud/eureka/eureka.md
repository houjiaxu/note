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
            DefaultEurekaServerContext类中有个initialize()是被@PostConstruct修饰的,也就是说此步创建的时候会调用initialize()这个方法
            DefaultEurekaServerContext#initialize
                // 启动一个线程，读取其他集群节点的信息，后面后续复制
                peerEurekaNodes.start();//启动一个只拥有一个线程的线程池,首次进来更新集群节点信息然后启动了一个定时线程，每60秒更新一次，也就是说后续可以根据配置动态的修改节点配置。（原生的spring cloud config支持）
                    updatePeerEurekaNodes(resolvePeerUrls());// 首次进来，更新集群节点信息
                registry.init(peerEurekaNodes);
                
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
    
    上面的配置类有个@Import(EurekaServerInitializerConfiguration.class)
    EurekaServerInitializerConfiguration#start里启动了一个线程,线程里做了如下事件
        // 初始化EurekaServer，同时启动Eureka Server
        eurekaServerBootstrap.contextInitialized(EurekaServerInitializerConfiguration.this.servletContext);
            initEurekaEnvironment();//初始化EurekaServer运行环境
            initEurekaServerContext();//初始化EurekaServer上下文
                EurekaServerContextHolder.initialize(this.serverContext);
                int registryCount = this.registry.syncUp();// 从相邻的eureka节点复制注册表
                    register(instance, instance.getLeaseInfo().getDurationInSecs(), true);//将其他节点的实例注册到本节点
                this.registry.openForTraffic(this.applicationInfoManager, registryCount);
                    默认每30秒发送心跳，1分钟就是2次,修改eureka状态为up,同时，这里面会开启一个定时任务，用于清理60秒没有心跳的客户端。自动下线
        
        publish(new EurekaRegistryAvailableEvent(getEurekaServerConfig()));// 发布EurekaServer的注册事件
        publish(new EurekaServerStartedEvent(getEurekaServerConfig()));
            发送Eureka Start 事件 ， 其他还有各种事件，我们可以监听这种时间，然后做一些特定的业务需求
         
Client端源码分析
    
    @Inject
    DiscoveryClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig config, 
        AbstractDiscoveryClientOptionalArgs args, Provider<BackupRegistry> backupRegistryProvider) {
        try {
            scheduler = Executors.newScheduledThreadPool(2,new ThreadFactoryBuilder().setNameFormat("DiscoveryClient‐%d").setDaemon(true).build());
        
            heartbeatExecutor = new ThreadPoolExecutor(1, clientConfig.getHeartbeatExecutorThreadPoolSize(), 0, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),
                new ThreadFactoryBuilder().setNameFormat("DiscoveryClient‐HeartbeatExecutor‐%d").setDaemon(true).build()); // use direct handoff
            
            cacheRefreshExecutor = new ThreadPoolExecutor(1, clientConfig.getCacheRefreshExecutorThreadPoolSize(), 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),new ThreadFactoryBuilder().setNameFormat("DiscoveryClient‐CacheRefreshExecutor‐%d").setDaemon(true).build()); // use direct handoff
            
            eurekaTransport = new EurekaTransport();
            scheduleServerEndpointTask(eurekaTransport, args);
        
            AzToRegionMapper azToRegionMapper;
            if (clientConfig.shouldUseDnsForFetchingServiceUrls()) {
                azToRegionMapper = new DNSBasedAzToRegionMapper(clientConfig);
            } else {
                azToRegionMapper = new PropertyBasedAzToRegionMapper(clientConfig);
            }
            if (null != remoteRegionsToFetch.get()) {
                azToRegionMapper.setRegionsToFetch(remoteRegionsToFetch.get().split(","));
            }
            instanceRegionChecker = new InstanceRegionChecker(azToRegionMapper, clientConfig.getRegion());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize DiscoveryClient!", e);
        }
        
        if (clientConfig.shouldFetchRegistry() && !fetchRegistry(false)) {
            fetchRegistryFromBackup();
        }
        
        // call and execute the pre registration handler before all background tasks (inc registration) started
        if (this.preRegistrationHandler != null) {
            this.preRegistrationHandler.beforeRegistration();
        }
        
        if (clientConfig.shouldRegisterWithEureka() && clientConfig.shouldEnforceRegistrationAtInit()) {
            if (!register() ) {
                throw new IllegalStateException("Registration error at startup. Invalid server response.");
            }
        }
        
        //最核心代码
        // finally, init the schedule tasks (e.g. cluster resolvers, heartbeat, instanceInfo replicator,fetch
        initScheduledTasks();
        
        Monitors.registerObject(this);
        
        // This is a bit of hack to allow for existing code using DiscoveryManager.getInstance()
        // to work with DI'd DiscoveryClient
        DiscoveryManager.getInstance().setDiscoveryClient(this);
        DiscoveryManager.getInstance().setEurekaClientConfig(config);
        
        initTimestampMs = System.currentTimeMillis();
    }
    
    https://blog.csdn.net/qq_34680763/article/details/123736997
    
    





















