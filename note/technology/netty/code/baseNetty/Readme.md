针对本目录下的代码进行如下说明:

    1）Bootstrap 和 ServerBootstrap 分别是客户端和服务器端的引导类，一个 Netty 应用程序通常由一个引导类开始，主要是用来配置整个 Netty 程序、设置业务处理类（Handler）、绑定端口、发起连接等。
    2）客户端创建一个 NioSocketChannel 作为客户端通道，去连接服务器。
    3）服务端首先创建一个 NioServerSocketChannel 作为服务器端通道，每当接收一个客户端连接就产生一个 NioSocketChannel 应对该客户端。
    4）使用 Channel 构建网络 IO 程序的时候，不同的协议、不同的阻塞类型和 Netty 中不同的 Channel 对应，常用的 Channel 有：
        NioSocketChannel：非阻塞的 TCP 客户端 Channel（本案例的客户端使用的 Channel）
        NioServerSocketChannel：非阻塞的 TCP 服务器端 Channel（本案例的服务器端使用的 Channel）
        NioDatagramChannel：非阻塞的 UDP Channel
        NioSctpChannel：非阻塞的 SCTP 客户端 Channel
        NioSctpServerChannel：非阻塞的 SCTP 服务器端 Channel

    默认情况下 BossGroup 和 WorkerGroup 都包含 16 个线程（NioEventLoop），这是因为我的 PC 是 8 核的 NioEventLoop 的数量=coreNum*2。这 16 个线程相当于主 Reactor。
    每一个 NioEventLoop 包含如下的属性（比如自己的 Selector、任务队列、执行器等）

