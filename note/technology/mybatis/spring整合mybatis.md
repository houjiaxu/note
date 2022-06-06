整合代码:
    建配置类, 内容如下
    @Configuration
    @MapperScan(basePackages = {"com.heal.mapper"})
    @EnableTransactionManagement
    public class MyBatisConfig{
       @Bean    //  <bean  class="org.mybatis.spring.SqlSessionFactoryBean">
       public SqlSessionFactoryBean sqlSessionFactory( ) throws IOException {
          SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
          factoryBean.setDataSource(dataSource());
          // 设置 MyBatis 配置文件路径
          factoryBean.setConfigLocation(new ClassPathResource("mybatis/mybatis-config.xml"));
          // 设置 SQL 映射文件路径
          factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/*.xml"));
          return factoryBean;
       }
       public DataSource dataSource() {
           DruidDataSource dataSource = new DruidDataSource();
           dataSource.setUsername("数据库账号");
           dataSource.setPassword("密码");
           dataSource.setDriverClassName("com.mysql.jdbc.Driver");
           dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis_example");
           return dataSource;
       }
    }

1.集成SqlSessionFactoryBean是干嘛的?

    就是用来构建SqlSessionFactory, 用来读取Mybatis的信息设置到Configuration里
    SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean
        1.首先看其实现方法getObject()
            先判断sqlSessionFactory是否存在, 不存在的话直接调用afterPropertiesSet(),存在则直接返回
        2.看其实现方法afterPropertiesSet()
            buildSqlSessionFactory();//通过sqlSessionFactoryBuilder来构建sqlSessionFactory
                Configuration targetConfiguration;声明一个Configuration对象用于保存mybatis的所有的配置信息
                判断当前的SqlSessionFactoryBean是否在配置@Bean的时候 factoryBean.setConfiguration();如果是,则把配置的SqlSessionFactoryBean配置的configuration 赋值给targetConfiguration
                如果下列对象不为空的话则设置到configuration对象中去:objectFactory / objectWrapperFactory / vfs
                scanClasses 别名处理,扫描别名包下class类型,注册到Configuration的别名映射器中
                targetConfiguration.addInterceptor(plugin);把我们自定义的插件注册到我们的mybatis的配置类上
                    Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed) 
                    ParameterHandler (getParameterObject, setParameters) 
                    ResultSetHandler (handleResultSets,  handleOutputParameters) 
                    StatementHandler (prepare, parameterize, batch, update, query)
                TypeHandlerRegistry#register扫描自定义的类型处理器(用来处理java类型和数据库类型的转化),并且注册
                LanguageRegistry().register(languageDriver);//MyBatis从3.2开始支持可插拔的脚本语言， 因此可以在插入一种语言的驱动（language driver）之后来写基于这种语言的动态SQL查询
                    具体用法:https://www.bookstack.cn/read/MyBatis-3.5.2/139653
                targetConfiguration.setDatabaseId //设置数据库厂商
                xmlConfigBuilder.parse(); //解析配置文件(mybatis-config.xml)到Configuration对象.
                targetConfiguration.setEnvironment(new Environment(this.environment, //为我们的configuration设置一个环境变量,此处利用SpringManagedTransactionFactory集成了spring事务
                    this.transactionFactory == null ? new SpringManagedTransactionFactory() : this.transactionFactory,
                    this.dataSource));
                循环mapper.xml文件调用xmlMapperBuilder.parse();进行解析
                sqlSessionFactoryBuilder.build(targetConfiguration);//构建SqlSessionFactory对象,默认是DefaultSqlSessionFactory
        3.明明注入的是SqlSessionFactoryBean,为什么构建时返回的是SqlSessionFactory类型呢? 
            是在某个地方做了转换,具体在哪儿忘求了
            
2.怎么集成Spring声明式事务?
    
    上面"1.集成SqlSessionFactoryBean是干嘛的?"中的"afterPropertiesSet()"中的设置环境变量targetConfiguration.setEnvironment中有个 SpringManagedTransactionFactory
    在使用事务的时候会调用SpringManagedTransactionFactory#newTransaction
        new SpringManagedTransaction(dataSource);
    在调用getConnection()时是调用SpringManagedTransaction#getConnection()
        openConnection();//开启一个连接
            DataSourceUtils.getConnection(this.dataSource);//这里是spring-jdbc里面的代码了
            ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(dataSource);
            conHolder.getConnection();
    实际上就是拿到Spring声明式事务,开启事务时创建的那个Connection,是放在TransactionSynchronizationManager.resources... 在构建SqlSessionFactoryBean时,
    会new 一个mybatis-spring适配的一个事务工厂类:SpringManagedTransactionFactory, 当mybatis获得Connection就会从这个SpringManagedTransactionFactory.getConnection中去获取
    Connection(这个Connection实际上就是TransactionSynchronizationManager中的Connection)
    
3.Mapper在Mybatis中的实现是什么? 如何能够把Mybatis的代理对象作为一个bean放入Spring容器中？
    Mapper类的jdk的动态代理实现: 
    
        从Map<Class<?>, MapperProxyFactory<?>> knownMappers中获取Mapper的代理工厂类MapperProxyFactory
            mapperProxyFactory.newInstance(sqlSession); //创建代理对象
                new MapperProxy这个类实现了InvocationHandler接口
                newInstance(mapperProxy);//调用jdk动态代理,进行实际的动态代理类的创建
                    Proxy.newProxyInstance

    科普: Spring中Bean的产生过程
        Spring启动过程中，大致会经过如下步骤去生成bean
        扫描指定的包路径下的class文件.根据class信息生成对应的BeanDefinition
        在此处，程序员可以利用某些机制去修改BeanDefinition
        根据BeanDefinition生成bean实例
        把生成的bean实例放入Spring容器中

4.根据Mapper创建好的代理怎么交给spring的ioc管理的?

    @MapperScan注解里的Import,有个MapperScannerRegistrar,其实现了ImportBeanDefinitionRegistrar接口
        看其实现方法registerBeanDefinitions,里面注册了一个MapperScannerConfigurer.class的bean定义
            BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                MapperScannerConfigurer实现了BeanDefinitionRegistryPostProcessor,BeanDefinitionRegistryPostProcessor继承自BeanFactoryPostProcessor
                也就是说在spring的refresh()方法里会调用这个MapperScannerConfigurer#postProcessBeanDefinitionRegistry
                    new ClassPathMapperScanner(registry);//创建一个Mybatis自定义的扫描器,这里之所以自定义是因为spring的扫描器是把接口过滤掉, 不注册成bean定义的,所以此处要重写isCandidateComponent方法,让接口也能被扫描
                    scanner.scan();//调用spring的扫描,把Mapper接口类也扫描进去,最终注册成bean定义.
                    
    里面注册了一个BeanFactoryPostProcessor,这里面大概是扫描了所有的mapper接口(自定义扫描器),然后利用FactoryBean创建了动态代理

mybatis plus源码也要看, 对比做了哪些扩展,怎么实现的.
看完这个就是springboot源码











