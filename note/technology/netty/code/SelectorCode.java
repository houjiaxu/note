//Selector 是一个抽象类,主要代码如下
public abstract class Selector implements Closeable {
    /**创建一个选择器对象*/
    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }
    /**返回所有发生事件的 Channel 对应的 SelectionKey 的集合，通过SelectionKey 可以找到对应的 Channel*/
    public abstract Set<SelectionKey> selectedKeys();

    /**返回所有 Channel 对应的 SelectionKey 的集合，通过 SelectionKey 可以找到对应的 Channel*/
    public abstract Set<SelectionKey> keys();

    /**监控所有注册的 Channel，当其中的 Channel 有 IO 操作可以进行时，将这些 Channel 对应的 SelectionKey 找到。参数用于设置超时时间*/
    public abstract int select(long timeout) throws IOException;

    /**无超时时间的 select ，一直等待，直到发现有 Channel 可以进行IO 操作*/
    public abstract int select() throws IOException;

    /**立即返回的 select */
    public abstract int selectNow() throws IOException;

    /**唤醒 Selector，对无超时时间的 select 过程起作用，终止其等待*/
    public abstract Selector wakeup();
}