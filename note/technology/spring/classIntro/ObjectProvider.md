[参考](https://blog.csdn.net/qq_43911324/article/details/125571293)

###1.简介
ObjectProvider<T>是spring4.3才有的新东西,专为注入时使用,允许编程可选性和宽松的非唯一处理

public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T>

注: 继承自ObjectFactory,内部获取实例还是通过bean工厂获取的.

主要方法:

    由工厂类返回一个(可能共享或独立的)实例对象, 允许指定显式的构造参数
    T getObject(Object... args)
    
    存在则返回, 不存在(不能被实例化)则返回null
    T getIfAvailable()
    
    存在则返回,不存在则返回defaultSupplier表达式,即该表达式理论上要返回一个T类型的对象,或者null.
    default T getIfAvailable(Supplier<T> defaultSupplier)
    
    若果对象存在,则执行dependencyConsumer表达式,即可以做一些处理.
    default void ifAvailable(Consumer<T> dependencyConsumer)
    
    返回一个实例,如果没有则返回null, 如果有多个,则返回标注了@primary的,即根据优先级返回.
    T getIfUnique()
    
    返回一个实例,如果为null 则执行defaultSupplier,即该表达式理论上要返回一个T类型的对象,或者null.
    default T getIfUnique(Supplier<T> defaultSupplier)
    
    若果对象存在,则执行dependencyConsumer表达式,即可以做一些处理.
    default void ifUnique(Consumer<T> dependencyConsumer)

###2.构造函数隐式注入,什么是隐式注入?

    @Service
    public class TestService {
        private final TestComponent testComponent;
        //Spring 4.3之前,如果想要使用TestService进行构造,则这一行必须加上@Autowired注解,否则就会查找无参构造器进行实例化,如果没有无参构造则报错
        //Spring 4.3之后,则无需使用@Autowired注解,Spring容器在启动时，创建TestService这个Bean的时候，会从BeanFactory中查找TestComponent这个Bean并注入进来。
        //那么问题来了,如果有多个构造函数呢? 会使用哪个? 优先级是什么? 既有无参又有有参构造,有参构造有多个,会怎么处理呢?
        //针对上面问题,在实例化之前,会调用后置处理器进行推断使用哪个构造方法,这个问题可以在那一步找到答案.
        public TestService(TestComponent testComponent) {
            this.testComponent = testComponent;
        }
    }

###3.隐式注入存在的问题
隐式注入并不完美，它存在强依赖,也就是有参构造函数里的参数必须存在.即require是true.而且另一个问题就是存在多个符合条件的bean的话,spring也会抛异常.
spring为了解决这个强依赖/多个符合条件bean的问题,在Spring 4.3中引入了ObjectProvider<T>接口

    @Service
    public class TestService {
        private final TestComponent testComponent;
        public TestService(ObjectProvider<TestComponent> testComponentObjectProvider) {
            //getIfUnique如果唯一,则进行注入.
            this.testComponent = testComponentObjectProvider.getIfUnique();
        }
    }

使用ObjectProvider<T>则可以避免强依赖导致的依赖对象不存在异常。如果容器中存在多个类型相同的Bean实例，ObjectProvider<T>中提供的方法可以根据Bean实现的
Ordered接口或者@Order注解指定的先后顺序获取一个Bean。这个过程的实现具体体现在DefaultListableBeanFactory#resolveDependency()方法中：


###4.Spring为什么要一直关注构造器注入?

如果依赖关系是强制的，那么最好使用构造函数进行注入。Spring官方这样推荐的理由是：
Spring团队通常提倡构造器注入，因为它可以将应用程序组件实现为不可变对象，并确保所需的依赖项不为空。此外，构造函数注入的组件总是以完全初始化的状态返回给客户端（调用）代码。

1.单一职责原则：当使用构造函数注入依赖时，开发者很容易识别参数是否过多。此时就需要考虑这个类的职责是否过大，是否需要拆分等问题。
而使用@Resource和@Autowired注解注入时，不容易发现与识别这个问题。(ps:但有些类就是职责比较多,尤其是controller类或者一些service类)

2.依赖不可变：使用构造器注入时，使用final关键字修饰field。(谁没事修改这个玩意干啥)

3.依赖不为空：使用构造器注入时，当要实例化一个Bean时，由于自己实现了有参构造函数，Spring不会调用默认构造函数，此时Spring容器注入所需的依赖，会存在两种情况：

    容器中存在所需要的Bean，直接注入。
    容器中不存在所需要的Bean，此时Spring会抛出异常。所以Spring会保证注入的依赖不为空。
    存在问题:构造器不能循环依赖,其实循环依赖是一个比较有争议的问题,有的说不应该有循环依赖,但实际上还是会有情况出现循环依赖的.比如订单服务和商品服务,
    一个订单有哪些商品和一个商品分别有哪些订单,这种情况就是要有循环依赖的.
4.完全初始化状态：这个点可以根据上面依赖不为空结合起来。Spring调用构造器实例化某个Bean时，要确保注入的依赖不为空，那么肯定会调用所依赖组件的构造方法完成
组件的实例化。所以Spring注入进来的依赖都是初始化后的状态。
