[OpenFeign之FeignClient动态代理生成原理](https://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484185&idx=1&sn=efb3a1f459be9970126269234ff813e7&chksm=cfd950d1f8aed9c7c9ec6bc8b00c376d9777aa6d6aa2b93ccf6a4b4376adbed8c4f3e1e3754b&scene=21#wechat_redirect)


[OpenFeign原来是这么基于Ribbon来实现负载均衡的](https://mp.weixin.qq.com/s?__biz=Mzg5MDczNDI0Nw==&mid=2247484211&idx=1&sn=13b1cb0832bfae9a6d2369193700fd19&chksm=cfd950fbf8aed9ed473a0e170480770c311f1b637607332a0df15f32e2e9a446f8bc97f0b295&scene=21#wechat_redirect)

@EnableFeignClinets作用源码剖析

    使用Feign，需要使用@EnableFeignClients，@EnableFeignClients的作用可以扫描指定包路径下的@FeignClient注解，也可以声明配置类；
    @EnableFeignClients注解其实就是整个feign的入口
    




























