/**
 * 事务监听器，重写执行本地事务方法以及事务回查方法
 * executeLocalTransaction：执行本地事务；
 * checkLocalTransaction：回查本地事务状态，根据这次回查的结果来决定此次事务是提交还是回滚；
 */
public class TransactionListenerImpl implements TransactionListener {

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String msgKey = msg.getKeys();
        switch (msgKey) {
            case "Num0":
            case "Num1":
                // 明确回复回滚操作，消息将会被删除，不允许被消费。
                return LocalTransactionState.ROLLBACK_MESSAGE;
            case "Num8":
            case "Num9":
                // 消息无响应,代表需要回查本地事务状态来决定是提交还是回滚事务
                return LocalTransactionState.UNKNOW;
            default:
                // 消息通过，允许消费者消费消息
                return LocalTransactionState.COMMIT_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        System.out.println("回查本地事务状态,消息Key: " + msg.getKeys() + ",消息内容: " + new String(msg.getBody()));
        // 需要根据业务，查询本地事务是否执行成功，这里直接返回COMMIT
        return LocalTransactionState.COMMIT_MESSAGE;
    }

}