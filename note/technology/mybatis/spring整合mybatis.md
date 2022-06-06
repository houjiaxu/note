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
                    scanner.scan();//调用ClassPathBeanDefinitionScanner#scan,把Mapper接口类也扫描进去,最终注册成bean定义.这样在spring创建bean的时候就会生成代理,然后放入容器
                        this.doScan(basePackages);//实际调用ClassPathMapperScanner#doScan
                            super.doScan(basePackages);//调用父类扫描器进行扫描(实际是spring里的扫描)
                            processBeanDefinitions(beanDefinitions);
                                正是在这里mybaits做了一个很牛逼的功能，将spring的bean定义玩到极致(做了偷天换日的操作) 我们知道通过父类扫描出来的mapper是接口类型的
                                比如我们com.tuling.mapper.UserMapper 他是一个接口 我们有基础的同学可能会知道我们的bean定义最终会被实例化成
                                对象，但是我们接口是不能实例化的,所以在processBeanDefinitions 来进行偷天换日
                                循环beanDefinitions
                                    definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);//通过构造器设置beanclass名称,这里要注意一下,接收的时候是个class,spring会根据name自动转换成class类
                                    definition.setBeanClass(this.mapperFactoryBeanClass);//将beanclass设置成MapperFactoryBean,这样一来spring在实例化的时候就会调用factorybean的getObject()方法了
                                    definition.getPropertyValues().add("sqlSessionFactory"//为Mapper对象绑定sqlSessionFactory引用
                                    definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                                        设置UserMapper<MapperFactoryBean>定义的注入模型是通过 包扫描进来的，所有我们的默认注入模型就是
                                        AutowireCapableBeanFactory.AUTOWIRE_NO=0注入模型为0的时候,在这种情况下,若我们的MapperFactoryBean
                                        的字段属性是永远自动注入不了值的因为字段上是没有 @AutoWired注解,所以我们需要把UserMapper<MapperFactoryBean> 的bean定义的注入模型给改成我们的 AUTOWIRE_BY_TYPE
                                        = 1,指定这个类型就是根据类型装配的话， 第一:我们的字段上不需要写@AutoWired注解，为啥? springioc会把当前UserMapper<MapperFactoryBean>中的setXXX(入参)
                                        都会去解析一次入参,入参的值可定会从ioc容器中获取，然后调用setXXX方法给赋值好.

    除了FactoryBean的方式之外(definition.setBeanClass(this.mapperFactoryBeanClass)),
        还可以使用工厂方法即definition.setBeanClass(自定义一个类.class);  definition.setFactoryMethodName("自定义类中的自定义一个方法")

    上面的一段代码明明注入的是SqlSessionFactory,为什么构建时返回的是SqlSessionFactoryBean类型呢(对应"整合代码"里的@Bean)?
        definition.getPropertyValues().add("sqlSessionFactory"//为Mapper对象绑定sqlSessionFactory引用
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        这个是在SqlSessionFactoryBean#getObjectType中做了个简单的转换,所以能够根据类型注入成功
            this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();


mybatis plus源码也要看, 对比做了哪些扩展,怎么实现的.
看完这个就是springboot源码











