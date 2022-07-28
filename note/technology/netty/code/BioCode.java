//服务端示例代码

public static void main(String[] args) throws IOException {
    ExecutorService threadPool = Executors.newCachedThreadPool();
    ServerSocket serverSocket = new ServerSocket(8080);
    //循环接收请求, 接收到的话,就使用线程池里的某个线程进行处理,也就是说,一个线程只能处理一个请求.
    while (true) {
        Socket socket = serverSocket.accept();
        threadPool.execute(() -> {
            handler(socket);
        });
    }
}

/**
 * 处理客户端请求
 */
private static void handler(Socket socket) throws IOException {
    byte[] bytes = new byte[1024];

    InputStream inputStream = socket.getInputStream();
    socket.close();

    while (true) {
        int read = inputStream.read(bytes);
        if (read != -1) {
            System.out.println("msg from client: " + new String(bytes, 0, read));
        } else {
            break;
        }
    }
}