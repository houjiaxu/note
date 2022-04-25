#设计模式简介
[设计模式简介-文档下方的评论也是精髓](https://www.runoob.com/design-pattern/design-pattern-intro.html)

**七大原则记忆口诀：开口里合最单依**

    开：开闭原则：实现热插拔，提高扩展性。
    口：接口隔离原则：降低耦合度，接口单独设计，互相隔离；
    里：里氏替换原则：实现抽象的规范，实现子父类互相替换；
    合：合成复用原则：尽量使用聚合，组合，而不是继承；
    最：最少知道原则（迪米特原则）又称不知道原则：功能模块尽量独立；
    单：单一职责原则
    依：依赖倒置原则：针对接口编程，实现开闭原则的基础；

#模式类型的区别

创建型模式-->对象怎么来

结构型模式-->对象和谁有关

行为型模式-->对象与对象在干嘛

J2EE 模式-->对象合起来要干嘛（表现层,文中表示层个人感觉用的不准确）java是面向对象的语言,所以要搞好对象,模式（套路）就是用来更加好的搞对象滴。


#设计模式类型事例
##1.创建型模式
[FACTORY 工厂模式](https://www.runoob.com/design-pattern/factory-pattern.html)
    
    说明: 
    加工工厂：给它“M4A1”，它给你产把警枪，给它“AK47”，你就端了把匪枪。CS里买枪的程序一定是用这个模式的。

[BUILDER 建造者](https://www.runoob.com/design-pattern/builder-pattern.html)

    生产流水线：以前是手工业作坊式的人工单个单个的生产零件然后一步一步组装做，好比有了工业革命，现在都由生产流水线代替了。如要造丰田汽车，先制定汽车的构造如由车胎、方向盘、发动机组成。再以此构造标准生产丰田汽车的车胎、方向盘、发动机。然后进行组装。最后得到丰田汽车；

[PROTOTYPE 原型模式](https://www.runoob.com/design-pattern/prototype-pattern.html)

    印刷术的发明：以前只能临贴抄写费时费力，效率极低，有了印刷术，突突的；

[SINGLETON 单例模式](https://www.runoob.com/design-pattern/singleton-pattern.html)

    确保唯一：不是靠new的，是靠instance的，而且要instance地全世界就这么一个实例(这可怜的类，也配叫“类”)。 看SingleTon类代码。

##2.结构型模式
[ADAPTER 适配器模式](https://www.runoob.com/design-pattern/adapter-pattern.html)

    翻译官：胡哥只会汉语，布什只会美语，翻译官既通汉又通美，Adapter了 ；

[DECORATOR 装饰器](https://www.runoob.com/design-pattern/decorator-pattern.html)

    说明: 装饰器本质上还是一个对象,是某个对象的实现,例如人(对象), 穿裤子的人(穿裤子的装饰器),穿T恤的人(穿T恤的装饰器),可以理解为既是人,又不是普通人/裸人.
         装饰器的功能又是对某个对象进行装饰,所以就造成了装饰器既是人,又对人进行装饰的现象,即所谓的给对象增加功能(装饰器持有该对象, 只有持有对象,才能增加功能)。
         但为什么不使用子类直接子类增加方法不就行了? 因为装饰器模式相比生成子类更为灵活,而且一旦功能多起来,那么子类就会显得很臃肿。
         所以装饰器的使用时机为可能扩展的功能很多很多很多,会导致子类显得非常臃肿的时候,当子类不臃肿的时候我们没必要使用该模式.
    装饰：名字可以标识一个人，为了表示对一个人的尊重，一般会称其为“尊敬的”，有了装饰，好看多了；

[BRIDGE 桥接](https://www.runoob.com/design-pattern/bridge-pattern.html)

    白马非马：马之颜色有黑白，马之性别有公母。我们说"这是马"太抽象，说"这是黑色的公马"又太死板，只有将颜色与性别和马动态组合，"这是（黑色的或白色的）（公或母）马"才显得灵活而飘逸，如此bridge模式精髓得矣。

[COMPOSITE 组合模式](https://www.runoob.com/design-pattern/composite-pattern.html)

    大家族：子又生孙，孙又生子，子子孙孙，无穷尽也，将众多纷杂的人口组织成一个按辈分排列的大家族即是此模式的实现；

[FACADE 外观模式](https://www.runoob.com/design-pattern/facade-pattern.html)

    求同存异：高中毕业需读初中和高中，博士也需读初中和高中，因此国家将初中和高中普及成九年制义务教育；

[FLYWEIGHT 享元模式](https://www.runoob.com/design-pattern/flyweight-pattern.html)

    一劳永逸：认识三千汉字，可以应付日常读书与写字，可见头脑中存在这个汉字库的重要；

[PROXY 代理模式](https://www.runoob.com/design-pattern/proxy-pattern.html)
    
    说明:为其他对象提供一种代理来控制对某个对象的访问。
        角色:接口,类,代理类. 接口可有可无
        只有一个接口也可创建代理类,不过前提是要知道这个代理类的功能,
        比如mapper,创建一个代理类之后,在代理类里写什么样的代码,就是mybatis框架里写的东西了
    垂帘听政：犹如清朝康熙年间的四大府臣，很多权利不在皇帝手里，必须通过辅佐大臣去办；

##3.行为模式
[CHAIN OF RESPONSIBLEITY 责任链](https://www.runoob.com/design-pattern/chain-of-responsibility-pattern.html) 

    租房：以前为了找房到处打听，效率低且找不到好的房源。现在有了房屋中介，于是向房屋中介提出租房请求，中介提供一个合适的房源，满意则不再请求，不满意继续看房，直到满意为止；

[COMMAND 命令模式](https://www.runoob.com/design-pattern/command-pattern.html)

    借刀杀人：以前是想杀谁就杀，但一段时间后领悟到，长此以往必将结仇太多，于是假手他人，挑拨他人之间的关系从而达到自己的目的；

[INTERPRETER 解释器](https://www.runoob.com/design-pattern/interpreter-pattern.html)

    文言文注释：一段文言文，将它翻译成白话文；

[ITERATOR 迭代器](https://www.runoob.com/design-pattern/iterator-pattern.html)

    赶尽杀绝：一个一个的搜索，绝不放掉一个；

[MEDIATOR 中介者](https://www.runoob.com/design-pattern/mediator-pattern.html)

    三角债：本来千头万绪的债务关系，忽出来一中介，包揽其一切，于是三角关系变成了独立的三方找第四方中介的关系；

[MEMENTO 备忘录](https://www.runoob.com/design-pattern/memento-pattern.html)

    有福同享：我有多少，你就有多少；

[OBSERVER 观察者](https://www.runoob.com/design-pattern/observer-pattern.html)

    看守者：一旦被看守者有什么异常情况，定会及时做出反应；

[STATE 状态模式](https://www.runoob.com/design-pattern/state-pattern.html)

    进出自由：如一扇门，能进能出，如果有很多人随时进进出出必定显得杂乱而安全，如今设一保安限制其进出，如此各人进出才显得规范；

[STRATEGY 策略模式](https://www.runoob.com/design-pattern/strategy-pattern.html)

    久病成良医：如人生病可以有各种症状，但经过长期摸索，就可以总结出感冒、肺病、肝炎等几种；

[TEMPLATE METHOD 模板方法](https://www.runoob.com/design-pattern/template-pattern.html)

    理论不一定要实践：教练的学生会游泳就行了，至于教练会不会则无关紧要；

[VISITOR 访问者](https://www.runoob.com/design-pattern/visitor-pattern.html)

    依法治罪：因张三杀人要被处死，李四偷窃要被罚款。由此势必制定处罚制度，故制定法律写明杀人、放火、偷窃等罪要受什么处罚，经通过后须变动要小。今后有人犯罪不管是谁，按共条例处罚即是，这就是访问者模式诞生的全过程。

#其他模式
[空对象模式](https://www.runoob.com/design-pattern/null-object-pattern.html)

[抽象工厂](https://www.runoob.com/design-pattern/abstract-factory-pattern.html) 

[委派模式]()

[MVC模式](https://www.runoob.com/design-pattern/mvc-pattern.html)

[业务代表模式](https://www.runoob.com/design-pattern/business-delegate-pattern.html)

[组合实体模式](https://www.runoob.com/design-pattern/composite-entity-pattern.html)

[数据访问对象模式](https://www.runoob.com/design-pattern/data-access-object-pattern.html)

[前端控制器模式](https://www.runoob.com/design-pattern/front-controller-pattern.html)

[拦截过滤器模式](https://www.runoob.com/design-pattern/intercepting-filter-pattern.html)

[服务定位器模式](https://www.runoob.com/design-pattern/service-locator-pattern.html)

[传输对象模式](https://www.runoob.com/design-pattern/transfer-object-pattern.html)


#设计模式的区别
有些设计模式看似功能是差不多的,那么到底有什么区别呢?

工厂方法 -> 策略: 工厂是创建型模式,而策略是行为型模式.

简单工厂 -> 工厂方法: 简单工厂是只有一个工厂,在工厂类里做判断, 工厂方法比简单工厂多了一个抽像工厂的角色.

工厂方法 -> 抽像工厂: 工厂方法中一个工厂只能生产1个种产品,抽象工厂中一个工厂能生产很多产品,比如华为工厂/小米工厂 都能生产手机/电脑.

建造者 -> 装饰者: 建造者是创建型模式,构建的过程必须是稳定的(例如:盖房子先打地基,再垒砖头), 装饰者是结构型模式,是对某个被"建造"出来的对象进行装饰,装饰的顺序不必稳定(例如对人进行装饰,可以先穿衣服再化妆,也可以先化妆再穿衣服)

代理模式 -> 适配器模式：适配器模式主要改变所考虑对象的接口，而代理模式不能改变所代理类的接口。 

代理模式 -> 装饰器模式：装饰器模式为了增强功能，而代理模式是为了加以控制。














