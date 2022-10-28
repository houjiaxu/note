JSqlParser是基于Javacc来开发的，借助于Javacc的能力来解析SQL文本；

###各个包说明

    net.sf.jsqlparser.expression  SQL构建相关类，比如EqualsTo、InExpression等表达式用于构建SQL。
    net.sf.jsqlparser.parser  SQL解析相关类，比如CCJSqlParserUtil。
    net.sf.jsqlparser.schema  主要存放数据库schema相关的类 ，比如表、列等。
    net.sf.jsqlparser.statement  封装了数据库操作对象，create、insert、delete、select、Drop等
    net.sf.jsqlparser.util   各种工具类、不同DB版本、SQL标准等处理类，如SelectUtils、DatabaseType等。

###常用Expression说明

    1、条件表达式,如：AndExpression（and），OrExpression(or)
    2、关系表达式,如：EqualsTo(=)，MinorThan(<)，GreaterThan(>)，……
    3、算术表达式,如：Addition（+），Subtraction（-），Multiplication（*）,Division(/),……
    4、列表达式,如：Column
    5、case表达式,如：CaseExpression
    6、值表达式,如：StringValue，DateValue,LongValue,DoubleValue,……
    7、函数表达式,如：Function
    8、参数表达式,如：JdbcParameter，JdbcNameParameter,……

###statement说明
    见的Statement有：Select,Create,Drop,Insert,Delete等，它们作为Statement实现类，均实现accept方法。
    是Visitor模式的典型应用，贯穿JSqlParser解析SQL语句的每个角落。
    Statement对应使用StatementVisitor进行解析,SelectItem使用SelectItemVisitor进行解析.....
    如果要针对SQL语句进行定制化处理，你只需实现StatementVisitor接口即可。

###常用Vistitor
    
    StatementVisitor,SelectVisitor,ExpressionVisitor,SelectItemVisitor,FromItemVisitor
    定制化解析具体某一块SQL语句时，需要定制相关的Visitor。

代码示例
    
    //0.解析一个sql
    Select select  = (Select) CCJSqlParserUtil.parse("select * from sys_dep");
    SelectBody selectBody  = select.getSelectBody();
    if (selectBody instanceof PlainSelect) {
        this.setWhere((PlainSelect)selectBody, args, dataScopeProperty);
    } else if (selectBody instanceof SetOperationList) {//复合,需要循环进行操作.
        SetOperationList setOperationList = (SetOperationList)selectBody;
        List<SelectBody> selects = setOperationList.getSelects();
        selects.forEach((body) -> {
            this.setWhere((PlainSelect)body, args, dataScopeProperty);
        });
    }
    
    public void setWhere(PlainSelect plainSelect, Object[] args, DataScopeProperty property){
        //1.解析一个sql
        Select select1  = (Select) CCJSqlParserUtil.parse("select department_id from sys_user");

        //循环所有的列
        List<DataColumnProperty> dataColumns = dataScopeProperty.getColumns();
        for (DataColumnProperty dataColumn : dataColumns) {
            //匹配列
            if ("department_id".equals(dataColumn.getName())) {
                //2.构建in条件
                List<Expression> userIds = new ArrayList<>();
                userIds.add(new StringValue("1"));
                //ItemsList表示条目,比如in(1,2,3)里的1,2,3,再比如insert (id,name)values(1,zhangsan)
                ItemsList itemsList = new ExpressionList(userIds);
                InExpression inExpression = new InExpression(new Column(dataColumn.getAliasDotName()), itemsList);
        
                //3.往原sql里塞组装的sql
                if (null == plainSelect.getWhere()) {
                    // 原来不存在 where 条件
                    plainSelect.setWhere(new Parenthesis(inExpression));
                } else {
                    // 原来存在 where 条件 and 处理
                    plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), inExpression));
                }
            }
        }
    }

like

    LikeExpression likeExpression = new LikeExpression();
    likeExpression.setLeftExpression(new Column(dataColumn.getAliasDotName()));
    likeExpression.setRightExpression(new StringValue("%1533%"));
equals

    EqualsTo equalsTo = new EqualsTo();
    equalsTo.setLeftExpression(new Column("id"));
    equalsTo.setRightExpression(new StringValue("1"));
    plainSelectBody.setWhere(equalsTo);

子查询SubSelect

    //1.构建一个查询
    Select innerSql = (Select) CCJSqlParserUtil.parse("select department_id from user");
    SelectBody selectBody = innerSql.getSelectBody();
    PlainSelect plainSelectBody = (PlainSelect) selectBody;

    //2.将查询作为一个子查询
    SubSelect subSelect = new SubSelect();
    subSelect.setSelectBody(plainSelectBody);

    //3.放入原sql中
    EqualsTo equalsTo1 = new EqualsTo();
    equalsTo1.setLeftExpression(new Column("department_id"));
    equalsTo1.setRightExpression(new Parenthesis(subSelect));
    if (null == plainSelect.getWhere()) {
        // 不存在 where 条件
        plainSelect.setWhere(equalsTo1);
    } else {
        // 存在 where 条件 and 处理
        plainSelect.setWhere(new AndExpression(plainSelect.getWhere(), equalsTo1));
    }

