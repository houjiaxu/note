/**对RealSubject进行代理**/
public interface Subject {
    public int sellBooks();
    public String speak();
}
public class RealSubject implements Subject{
    @Override
    public int sellBooks() {
        System.out.println("卖书");
        return 1 ;
    }
    @Override
    public String speak() {
        System.out.println("说话");
        return "张三";
    }
}

/**代理**/
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
public class MyInvocationHandler implements InvocationHandler {
    /**因为需要处理真实角色，所以要把真实角色传进来*/
    Subject realSubject ;
    public MyInvocationHandler(Subject realSubject) {
        this.realSubject = realSubject;
    }
    /** proxy代理类, method正在调用的方法,args方法的参数**/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("调用代理类");
        if(method.getName().equals("sellBooks")){
            int invoke = (int)method.invoke(realSubject, args);
            System.out.println("调用的是卖书的方法");
            return invoke ;
        }else {
            String string = (String) method.invoke(realSubject,args) ;
            System.out.println("调用的是说话的方法");
            return  string ;
        }
    }
}
/**调用**/
public class Client {
    public static void main(String[] args) {
        //真实对象
        Subject realSubject =  new RealSubject();
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler(realSubject);
        //代理对象: newProxyInstance的第二个参数是接口的class
        Subject proxyClass = (Subject) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Subject.class}, myInvocationHandler);
        proxyClass.sellBooks();
        proxyClass.speak();
    }
}