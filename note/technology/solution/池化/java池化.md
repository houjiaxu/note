[Java池化技术](https://mp.weixin.qq.com/s/cMpOY6mGj1RnX2fFr22IYQ)

###定义及思考
池化:使用一个虚拟的池子，将这些资源保存起来，当使用的时候，池子里快速获取一个即可。常见的有数据库连接池、线程池、常量池、对象池等

分为资源和池

    1.资源: 直接拿省性能从而提高响应, 不用自己创建降低使用复杂度;
    2.池: 可控制池子的大小,方便管理


###背景
使用池化技术的场景:

    1.对象的创建成本过大,并且通过轻量级的重置工作，可以循环、重复地使用。
    2.想要控制数量,比如线程池可以控制线程数,连接池可以控制链接数量.


###线程池
[线程池灵魂8连问](https://mp.weixin.qq.com/s/nj9tnz1JI3ZsLeuk8S5DTg)

1.日常工作中有用到线程池吗？什么是线程池？为什么要使用线程池？

    线程池是为了更方便的管理线程、并行执行任务。

2.ThreadPoolExecutor 都有哪些核心参数？

    核心线程数（corePoolSize）、最大线程数（maximumPoolSize），空闲线程超时时间（keepAliveTime）、时间单位（unit）、
    阻塞队列（workQueue）、拒绝策略（handler）、线程工厂（ThreadFactory）
    顺便把执行流程也说一下

3.什么是阻塞队列？说说常用的阻塞队列有哪些？

    阻塞:当从阻塞队列中获取数据时，如果队列为空，则等待,如果队列已满，则等待直到队列中有元素被移除。

    1）ArrayBlockingQueue：由数组实现的有界阻塞队列，该队列按照 FIFO 对元素进行排序。维护两个整形变量，标识队列头尾在数组中的位置，在生产者放入
        和消费者获取数据共用一个锁对象，意味着两者无法真正的并行运行，性能较低。
    2）LinkedBlockingQueue：由链表组成的有界阻塞队列，如果不指定大小，默认使用 Integer.MAX_VALUE 作为队列大小，该队列按照 FIFO 对元素进行
        排序，对生产者和消费者分别维护了独立的锁来控制数据同步，意味着该队列有着更高的并发性能。
    3）SynchronousQueue：不存储元素的阻塞队列，无容量，可以设置公平或非公平模式，插入操作必须等待获取操作移除元素，反之亦然。
    4）PriorityBlockingQueue：支持优先级排序的无界阻塞队列，默认情况下根据自然序排序，也可以指定 Comparator。
    5）DelayQueue：支持延时获取元素的无界阻塞队列，创建元素时可以指定多久之后才能从队列中获取元素，常用于缓存系统或定时任务调度系统。
    6）LinkedTransferQueue：一个由链表结构组成的无界阻塞队列，与LinkedBlockingQueue相比多了transfer和tryTranfer方法，该方法在有消费者等待接收元素时会立即将元素传递给消费者。
    7）LinkedBlockingDeque：一个由链表结构组成的双端阻塞队列，可以从队列的两端插入和删除元素。

4.ThreadPoolExecutor 都用到了哪些锁？为什么要用锁？

1）mainLock 锁:
    ThreadPoolExecutor 内部维护了 ReentrantLock 类型锁 mainLock，在访问 workers 成员变量以及进行相关数据统计记账（比如访问 largestPoolSize、completedTaskCount）时需要获取该重入锁。

    private final ReentrantLock mainLock = new ReentrantLock();
    /** Set containing all worker threads in pool. Accessed only when holding mainLock. */
    private final HashSet<Worker> workers = new HashSet<Worker>();
    /** Tracks largest attained pool size. Accessed only under mainLock. */
    private int largestPoolSize;
    /**  Counter for completed tasks. Updated only on termination of worker threads. Accessed only under mainLock.*/
    private long completedTaskCount;

    面试官：为什么要有 mainLock？
    workers 变量用的 HashSet 是线程不安全的，是不能用于多线程环境的。largestPoolSize、completedTaskCount 也是没用 volatile 修饰，所以需要在锁的保护下进行访问。

    面试官：为什么不直接用个线程安全容器呢？
    其实 Doug 老爷子在 mainLock 变量的注释上解释了，意思就是说事实证明，相比于线程安全容器，此处更适合用 lock，主要原因之一就是串行化 interruptIdleWorkers() 方法，避免了不必要的中断风暴
    
    面试官：怎么理解这个中断风暴呢？
    其实简单理解就是如果不加锁，interruptIdleWorkers() 方法在多线程访问下就会发生这种情况。一个线程调用interruptIdleWorkers() 方法对 Worker 进行中断，
        此时该 Worker 出于中断中状态，此时又来一个线程去中断正在中断中的 Worker 线程，这就是所谓的中断风暴。
    
    面试官：那 largestPoolSize、completedTaskCount 变量加个 volatile 关键字修饰是不是就可以不用 mainLock 了？
    这个其实 Doug 老爷子也考虑到了，其他一些内部变量能用 volatile 的都加了 volatile 修饰了，这两个没加主要就是为了保证这两个参数的准确性，在获取这两个值时，
        能保证获取到的一定是修改方法执行完成后的值。如果不加锁，可能在修改方法还没执行完成时，此时来获取该值，获取到的就是修改前的值。

2）Worker 线程锁:Worker 线程继承 AQS，实现了 Runnable 接口，内部持有一个 Thread 变量，一个 firstTask，及 completedTasks 三个成员变量。

    基于 AQS 的 acquire()、tryAcquire() 实现了 lock()、tryLock() 方法，类上也有注释，该锁主要是用来维护运行中线程的中断状态。在 runWorker() 方法中以及刚说的 interruptIdleWorkers() 方法中用到了。

    面试官：这个维护运行中线程的中断状态怎么理解呢？

    protected boolean tryAcquire(int unused) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }
    public void lock()        { acquire(1); }
    public boolean tryLock()  { return tryAcquire(1); }

    在runWorker() 方法中获取到任务开始执行前，需要先调用 w.lock() 方法，lock() 方法会调用 tryAcquire() 方法，tryAcquire() 实现了一把非重入锁，通过 CAS 实现加锁。
    interruptIdleWorkers() 方法会中断那些等待获取任务的线程，会调用 w.tryLock() 方法来加锁，如果一个线程已经在执行任务中，那么 tryLock() 就获取锁失败，就保证了不能中断运行中的线程了。
    所以 Worker 继承 AQS 主要就是为了实现了一把非重入锁，维护线程的中断状态，保证不能中断运行中的线程。

5.你在项目中是怎样使用线程池的？Executors 了解吗？

    现在大多数公司都在遵循阿里巴巴 Java 开发规范，该规范里明确说明不允许使用 Executors 创建线程池，而是通过 ThreadPoolExecutor 显示指定参数去创建

    Executors.newFixedThreadPool 和 Executors.SingleThreadPool 创建的线程池内部使用的是无界（Integer.MAX_VALUE）的 LinkedBlockingQueue 队列，可能会堆积大量请求，导致 OOM
    Executors.newCachedThreadPool 和Executors.scheduledThreadPool 创建的线程池最大线程数是用的Integer.MAX_VALUE，可能会创建大量线程，导致 OOM

    在日常工作中也有封装类似的工具类，但是都是内存安全的，参数需要自己指定适当的值，也有基于 LinkedBlockingQueue 实现了内存安全阻塞队列 MemorySafeLinkedBlockingQueue，
        当系统内存达到设置的剩余阈值时，就不在往队列里添加任务了，避免发生 OOM

    可以使用 Spring 提供的 ThreadPoolTaskExecutor，或者 DynamicTp 框架提供的 DtpExecutor 线程池实现。

    按业务类型进行线程池隔离，各任务执行互不影响，避免共享一个线程池，任务执行参差不齐，相互影响，高耗时任务会占满线程池资源，导致低耗时任务没机会执行；同时如果任务之间存在父子关系，可能会导致死锁的发生，进而引发 OOM。

6.线程池核心参数设置多少合适呢？

    《Java并发编程事件》这本书里介绍的一个线程数计算公式：
        Ncpu = CPU 核数
        Ucpu = CPU 目标利用率
        W / C = 等待时间 / 计算时间的比例
    要程序跑到 CPU 的目标利用率，需要的线程数为：Nthreads = Ncpu * Ucpu * (1 + W / C)
    但过于偏向理论,所以实际还是cp密n+1, io密2n;
    然后通过压测不断的动态调整线程池参数，观察 CPU 利用率、系统负载、GC、内存、RT、吞吐量 等各种综合指标数据，来找到一个相对比较合理的值。

7.你们线程池是咋监控的？

    我们自己对线程池 ThreadPoolExecutor 做了一些增强，做了一个线程池管理框架。主要功能有监控告警、动态调参。主要利用了 ThreadPoolExecutor 类提供的一些 set、get方法以及一些钩子函数。
    动态调参是基于配置中心实现的，核心参数配置在配置中心，可以随时调整、实时生效，利用了线程池提供的 set 方法。
    监控，主要就是利用线程池提供的一些 get 方法来获取一些指标数据，然后采集数据上报到监控系统进行大盘展示。也提供了 Endpoint 实时查看线程池指标数据。
    同时定义了5种告警规则:
        线程池活跃度告警。活跃度 = activeCount / maximumPoolSize，当活跃度达到配置的阈值时，会进行事前告警。
        队列容量告警。容量使用率 = queueSize / queueCapacity，当队列容量达到配置的阈值时，会进行事前告警。
        拒绝策略告警。当触发拒绝策略时，会进行告警。
        任务执行超时告警。重写 ThreadPoolExecutor 的 afterExecute() 和 beforeExecute()，根据当前时间和开始时间的差值算出任务执行时长，超过配置的阈值会触发告警。
        任务排队超时告警。重写 ThreadPoolExecutor 的  beforeExecute()，记录提交任务时时间，根据当前时间和提交时间的差值算出任务排队时长，超过配置的阈值会触发告警
    通过监控+告警可以让我们及时感知到我们业务线程池的执行负载情况，第一时间做出调整，防止事故的发生。

8.使用线程池的过程中遇到过哪些坑或者需要注意的地方？

    1）OOM 问题。刚开始使用线程都是通过 Executors 创建的，前面说了，这种方式创建的线程池会有发生 OOM 的风险。
    2）任务执行异常丢失问题。可以通过下述4种方式解决
        在任务代码中增加 try、catch 异常处理
        如果使用的 Future 方式，则可通过 Future 对象的 get 方法接收抛出的异常
        为工作线程设置 setUncaughtExceptionHandler，在 uncaughtException 方法中处理异常
        可以重写 afterExecute(Runnable r, Throwable t) 方法，拿到异常 t
    3）共享线程池问题。整个服务共享一个全局线程池，导致任务相互影响，耗时长的任务占满资源，短耗时任务得不到执行。同时父子线程间会导致死锁的发生，今儿导致 OOM
    4）跟 ThreadLocal 配合使用，导致脏数据问题。我们知道 Tomcat 利用线程池来处理收到的请求，会复用线程，如果我们代码中用到了 ThreadLocal，
        在请求处理完后没有去 remove，那每个请求就有可能获取到之前请求遗留的脏值。
    5）ThreadLocal 在线程池场景下会失效，可以考虑用阿里开源的 Ttl 来解决

9.DynamicTp介绍:

    DynamicTp 是一个基于配置中心实现的轻量级动态线程池管理工具，主要功能可以总结为动态调参、通知报警、运行监控、三方包线程池管理等几大类。
    代码零侵入：所有配置都放在配置中心，对业务代码零侵入
    轻量简单：基于 springboot 实现，引入 starter，接入只需简单4步就可完成，顺利3分钟搞定
    高可扩展：框架核心功能都提供 SPI 接口供用户自定义个性化实现（配置中心、配置文件解析、通知告警、监控数据采集、任务包装等等）
    线上大规模应用：参考美团线程池实践（https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html），美团内部已经有该理论成熟的应用经验
    多平台通知报警：提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），已支持企业微信、钉钉、飞书报警，同时提供 SPI 接口可自定义扩展实现
    监控：定时采集线程池指标数据，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现
    任务增强：提供任务包装功能，实现TaskWrapper接口即可，如 MdcTaskWrapper、TtlTaskWrapper、SwTraceTaskWrapper，可以支持线程池上下文信息传递
    兼容性：JUC 普通线程池和 Spring 中的 ThreadPoolTaskExecutor 也可以被框架监控，@Bean 定义时加 @DynamicTp 注解即可
    可靠性：框架提供的线程池实现 Spring 生命周期方法，可以在 Spring 容器关闭前尽可能多的处理队列中的任务
    多模式：参考Tomcat线程池提供了 IO 密集型场景使用的 EagerDtpExecutor 线程池
    支持多配置中心：基于主流配置中心实现线程池参数动态调整，实时生效，已支持 Nacos、Apollo、Zookeeper、Consul、Etcd，同时也提供 SPI 接口可自定义扩展实现
    中间件线程池管理：集成管理常用第三方组件的线程池，已集成Tomcat、Jetty、Undertow、Dubbo、RocketMq、Hystrix等组件的线程池管理（调参、监控报警）





你平常会把连接池设置成多大呢？

数据库连接池的大小设置得越大越好? 有的同学甚至把这个值设置成 1000 以上，这是一种误解。根据经验，数据库连接，只需要 20~50 个就够用了。具体的大小，要根据业务属性进行调整，但大得离谱肯定是不合适的。







