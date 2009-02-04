package temporary;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Jan 19, 2009
 * Time: 2:33:19 PM
 * To change this template use File | Settings | File Templates.
 */

abstract class A {
    public abstract void msg();
}

class B extends A {

    public void msg() {
        System.out.println("Hello from class B");
    }
}

class C extends A {

    public void msg() {
        System.out.println("Hello from class C");
    }

    public int func2() { return 2; }
}

class B2 extends B {
    public void msg() {
        System.out.println("Hello from B2");
    }
}

public class htest {
    public static void process(A object) {
         object.msg();
    }

    public static void proc(B2 object) {
        object.msg();
    }
    public static void main(String[] args) {
        B b1 = new B();
        B2 b2 = new B2();
        process(b1);
        process(b2);

        proc(b2);
        proc((B2) b1);

     }
}

