#好用的Function接口?

在java.util.function中全都是:以下Function接口都有现成的子类型的实现

Supplier<T>  T get(); 没有参数,有返回

Function<T, R>  R apply(T t);  1个参数,有返回

BiFunction<T, U, R>   R apply(T t, U u);  2个参数,有返回

Consumer<T>  void accept(T t);   1个参数,无返回

BiConsumer<T, U>  void accept(T t, U u);  2个参数,无返回

Predicate<T>  boolean test(T t); 1个参数,返回boolean

BiPredicate<T, U>  boolean test(T t, U u); 2个参数,返回boolean

HashMap的rehash的结果要么是等于其在旧数组时的索引位置,记为低位区链表lo开头-low;
要么是在旧数组时的索引位置再加上旧数组长度，记为高位区链表hi开头high.