1.集成SqlSessionFactoryBean是干嘛的?
    就是用来构建SqlSessionFactory, 用来读取Mybatis的信息设置到Configuration里
2.怎么集成Spring声明式事务?
    实际上就是拿到Spring声明式事务,开启事务时创建的那个Connection,是放在TransactionSynchronizationManager.resources... 在构建SqlSessionFactoryBean时,
    会new 一个mybatis-spring适配的一个事务工厂类:SpringManagedTransactionFactory, 当mybatis获得Connection就会从这个SpringManagedTransactionFactory.getConnection中去获取
    Connection(这个Connection实际上就是TransactionSynchronizationManager中的Connection)
3.Mapper在Mybatis中的实现是什么?
    jdk的动态代理实现,这块看下代码
4.根据Mapper创建好的代理怎么交给spring的ioc管理的?
    @MapperScan注解里的Import,里面注册了一个BeanFactoryPostProcessor,这里面大概是扫描了所有的mapper接口(自定义扫描器),然后利用FactoryBean创建了动态代理












