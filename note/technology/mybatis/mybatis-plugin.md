###1.mybatis插件如何拿到参数or执行结果?

    //1.拿到拦截的对象,通过对象拿到MappedStatement,以下2个例子
    //1.1 拦截Executor
    Invocation invocation
    Object[] args = invocation.getArgs();//参数
    MappedStatement statement = (MappedStatement) args[0];  
    //1.2 拦截ResultSetHandler
    Object resultSetHandler = invocation.getTarget();
    DefaultResultSetHandler defaultResultSetHandler = (DefaultResultSetHandler)resultSetHandler;
    Field field = defaultResultSetHandler.getClass().getDeclaredField("mappedStatement");
    field.setAccessible(true);
    MappedStatement mappedStatement = (MappedStatement)field.get(defaultResultSetHandler);
    //2.拿到MappedStatement可以将原对象封装成MetaObject,通过MetaObject的get/setvalue方法可以间接操作原对象.
    Configuration configuration = statement.getConfiguration();
    MetaObject metaObject = configuration.newMetaObject(obj);//此处的obj在执行前则是参数,执行后则是执行后的结果.
    metaObject.getValue(属性名)
















