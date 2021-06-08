package tudbut.mod.client.ttc.utils;

public class ThreadManager { // Self-explanatory
    public static Thread run(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.start();
        return t;
    }
    public static Thread run(String name, Runnable runnable) {
        Thread t = new Thread(runnable, name);
        t.start();
        return t;
    }
}
