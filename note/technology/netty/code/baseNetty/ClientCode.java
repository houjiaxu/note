/**
 * 需要的依赖：
 * <dependency>
 * <groupId>io.netty</groupId>
 * <artifactId>netty-all</artifactId>
 * <version>4.1.52.Final</version>
 * </dependency>
 */
public static void main(String[] args) throws InterruptedException {

        // 客户端只需要一个事件循环组，可以看做 BossGroup
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            // 创建客户端的启动对象
            Bootstrap bootstrap = new Bootstrap();
            // 配置参数
            bootstrap.group(eventLoopGroup)// 设置线程组
            .channel(NioSocketChannel.class)// 说明客户端通道的实现类（便于 Netty 做反射处理）
            // handler()方法用于给 BossGroup 设置业务处理器
            .handler(
                new ChannelInitializer<SocketChannel>() {// 创建一个通道初始化对象
                    // 向 Pipeline 添加业务处理器
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                        // 可以继续调用 socketChannel.pipeline().addLast()添加更多 Handler
                    }
            });

            System.out.println("client is ready...");

            // 启动客户端去连接服务器端，ChannelFuture 涉及到 Netty 的异步模型，后面展开讲
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1",8080).sync();
            // 对通道关闭进行监听
            channelFuture.channel().closeFuture().sync();

        } finally {
            eventLoopGroup.shutdownGracefully();
        }
}