[说明文档](https://baomidou.com/pages/24112f/)
###常要处理的类型

    1.外层类: map  list set  数组
    2.实际对象: int  short long byte boolean char double float Object(用户自定义的类)

###mybatisplus是如何将自动装配插件的?

    MybatisAutoConfiguration该类是mybatis自动配置类
    该类中有个构造方法MybatisAutoConfiguration(有一堆参数),里面包括了ObjectProvider<Interceptor[]> interceptorsProvider,
    而ObjectProvider是spring的一个类,继承自ObjectFactory,其getIfAvailable方法在多个beanfactory类中进行实现
    例如DefaultListableBeanFactory,大致愿意就是解析成bean定义,然后进行注册.
    也就不用我们手动注册插件了,也不用将插件手动添加到mybatis配置当中去了

###mybatisplus 源码解析

    参考: https://www.cnblogs.com/jelly12345/p/15628277.html
    mybatisXMLConfigBuilder.parse()解析xml文件
        parseConfiguration解析成configuration
            pluginElement(root.evalNode("plugins"));//解析插件,生成拦截器实例,放入拦截器列表.
            mapperElement
                mapperParser.parse();实际是XMLMapperBuilder#parse
                    bindMapperForNamespace();
                        configuration.addMapper(boundType);此处的configuration实际是mybatis plus继承mybatis原有的Configuration类的新类MybatisConfiguration。
                            mybatisMapperRegistry.addMapper(type);
                                parser.parse();实际是调用MybatisMapperAnnotationBuilder#parse方法
                                    parserInjector();
                                        GlobalConfigUtils.getSqlInjector(configuration).inspectInject(assistant, type);//实际是DefaultSqlInjectorSQL#inspectInject
                                        DefaultSqlInjectorSQL#inspectInject的方法是从抽象类中继承来的,抽象类调用了getMethodList方法,该方法由子类实现,
                                        故调用的是DefaultSqlInjectorSQL#getMethodList注入了这些方法.






