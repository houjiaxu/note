###几个主要对象间的关系
    MappedStatement 可以getBoundSql, 里面的boundsql是从SqlSource来的
    BoundSql可以get到具体的sql信息
    Configuration 整个的"配置"以及解析后的MappedStatement
###mybatis插件如何拿到参数or执行结果?

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

###一个sql在mysql的处理过程,即一个mapper.xml的<select>节点在Mybatis经历了什么?

    1.SqlSessionFactoryBuilder().build()的时候进行解析mapper.xml节点,并构建成MappedStatement放入Configuration#mappedStatements中.
        主要方法在XMLStatementBuilder#parseStatementNode,详细链路参考"源码.md"即可.
        如何构建的?
            //1.构建成DynamicSqlSource,其rootSqlNode是将sql解析成多个部分,比如foreachSqlNode,staticTextSqlNode等等.
                SqlSource sqlSource = langDriver.createSqlSource(this.configuration, this.context, parameterTypeClass);
            //2.调用MapperBuilderAssistant#addMappedStatement转换成MappedStatement对象,主要是使用MappedStatement.Builder进行构建,该方法核心代码如下:
                org.apache.ibatis.mapping.MappedStatement.Builder statementBuilder =
                    (new org.apache.ibatis.mapping.MappedStatement.Builder(this.configuration, id, sqlSource, sqlCommandType))
                    .resource(this.resource)
                    .fetchSize(fetchSize)
                    .timeout(timeout)
                    .statementType(statementType)
                    .keyGenerator(keyGenerator)
                    .keyProperty(keyProperty)
                    .keyColumn(keyColumn)
                    .databaseId(databaseId)
                    .lang(lang)
                    .resultOrdered(resultOrdered)
                    .resultSets(resultSets)
                    .resultMaps(this.getStatementResultMaps(resultMap, resultType, id))
                    .resultSetType(resultSetType)
                    .flushCacheRequired((Boolean)this.valueOrDefault(flushCache, !isSelect))
                    .useCache((Boolean)this.valueOrDefault(useCache, isSelect))
                    .cache(this.currentCache);
                MappedStatement statement = statementBuilder.build();
    
    2.执行,根据statementId找到具体的MappedStatement,然后调用executor.query方法执行.
        configuration.getMappedStatement(statement)

###做插件的本质
    当我们需要对sql做改造的时候,就是在执行之前,对MappedStatement进行重组,然后重新构建.
    重组的时候就是对实际的sql进行重组,
    MappedStatement.Builder builder = new MappedStatement.Builder(mappedStatement.getConfiguration(),
        mappedStatement.getId(), sqlSource, mappedStatement.getSqlCommandType());
    注意此步的sqlSource,就是我们要操作的sql就在这里面,也就是我们要自定义一个sqlsource(默认实现是DynamicSqlSource),在其getBoundsql时返回我们重组后的sql即可.
    可以参考mybatis-mate插件的数据权限
    另外: 解析or操作sql的时候可以使用开源框架jssqlparser,是com.github.jsqlparser jsqlparser 4.4












