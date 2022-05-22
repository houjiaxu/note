#JDK升级
升到了jdk8

#junit
加了很多新的日志注解

#日志
jdk1.3之前没有任何日志框架,只能靠System.out

随后出现log4j,并开源了,出现了好多开源版本.

再往后sun公司的jdk1.4的时候开发除了JUL框架, 即java.util.logging

此时出现了log4j, JUL, jboss-logging..多个框架,那么应该怎么选日志框架呢? 或者集成多个日志框架的时候就冲突了

apache软件基金会出面研究出了日志门面: jcl jakarta common logging, 日志门面值提供适配, 不提供具体的实现,上面的log4j,JUL等是实现

后来开发jcl的人又开发出了slf4j 日志门面, 现在slf4j市场占有率是较高的, 然后开发出了日志框架logback. apache又开发了log4j2

现在日志一般使用slf4j + log4j2  或者 slf4j + logback

spring4是使用的jcl + JUL,spring5使用jcl + log4j2 / slf4j 
spring4里面如果要使用jcl + 别的日志框架,需要加一个适配包,比如使用slf4j,则需要加jcl-over-slf4j.jar,而spring5就自己适配了,不用额外加包





































