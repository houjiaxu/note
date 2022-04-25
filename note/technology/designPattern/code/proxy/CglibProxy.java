/**为没有实现接口的类做代理**/
public class Engineer {
    // 可以被代理
    public void eat() {
        System.out.println("工程师正在吃饭");
    }
    public final void work() {//final
        System.out.println("工程师正在工作");
    }
    private void play() {//private
        System.out.println("this engineer is playing game");
    }
}
/**代理**/
public class CglibProxy implements MethodInterceptor {
    private Object target;
    public CglibProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("### before invocation");
        Object result = method.invoke(target, objects);
        System.out.println("### end invocation");
        return result;
    }

    public static Object getProxy(Object target) {
        Enhancer enhancer = new Enhancer();
        // 设置需要代理的对象
        enhancer.setSuperclass(target.getClass());
        // 设置代理人
        enhancer.setCallback(new CglibProxy(target));
        return enhancer.create();//这一步调用的时候,会调用上方重写的intercept方法
    }
}

public class CglibMainTest {
    public static void main(String[] args) {
        // 生成 Cglib 代理类
        Engineer engineerProxy = (Engineer) CglibProxy.getProxy(new Engineer());
        engineerProxy.eat();
    }
}























