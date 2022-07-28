//服务端示例
//工作过程参考(img/img.png)
public static void main(String[] args) throws IOException {
        //1.创建Selector和Channel对象
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));// channel绑定端口
        serverSocketChannel.configureBlocking(false);//设置为非阻塞模式

        //2.绑定Channel和selector，关注 OP_ACCEPT 事件;  一个selector可以绑定多个channel的
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //3.轮训查询channel是否有请求数据,然后进行处理
        while (true) {
            //查询channel是否有数据,没有数据则返回0
            if (selector.select(1000) == 0) {
                continue;
            }

            //处理请求，找到 Channel 对应的 SelectionKey 的集合
            // SelectionKey 和 Selector 关联（即通过 SelectionKey 可以找到对应的 Selector），
            // 也和 SocketChannel 关联（即通过 SelectionKey 可以找到对应的 SocketChannel）
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                // 发生 OP_ACCEPT 事件，处理连接请求
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // 将 socketChannel 也注册到 selector，关注 OP_READ事件，并给 socketChannel 关联 Buffer
                    //这一步注册到子reactor上.
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                // 发生 OP_READ 事件，读客户端数据
                if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                    channel.read(buffer);
                    System.out.println("msg form client: " + new String(buffer.array()));
                }

                // 手动从集合中移除当前的 selectionKey，防止重复处理事件
                iterator.remove();
            }
        }
}