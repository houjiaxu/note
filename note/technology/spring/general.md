#常识
https://note.youdao.com/web/#/file/WEBdcc03127c1a4d9cd16dde229ebb621f3/note/WEBb1a023445c3f892e9866e560f83b2c19/


spring中的BeanFactory使用的是简单工厂模式,主要责任是负责生产bean

所有解析器，都是对 BeanDefinitionParser 接口的统一实现，入口都是从 parse 函数开始的


发布的时候,要先让机器上正在处理的请求运行完毕,也就是先让请求打到其他机器上, 然后再重启该机器的容器. 要不然其他请求过来直接报错.

在bean的销毁方法中getbean了,想这么做的话, spring有扩展点, 可以指定销毁前和销毁后要执行的逻辑
停止项目运行的时候也要打断点看下,整个流程时怎么样的.


-------------------------------容器引入bean的几种方式---------------------------------------
@Bean
@Import({HiService.class})  可写入多个
@Import({MyImportSelector.class})   MyImportSelector implements ImportSelector
@Import({MyImportBeanDefinitionRegistrar.class})   MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar 
-------------------------------bean属性改成配置---------------------------------------
@Value 写到一个个属性,支持SpEL,不支持JSR303数据校验	
@ConfigurationProperties 直接写到bean类上即可,不支持SpEL,支持JSR303数据校验	



