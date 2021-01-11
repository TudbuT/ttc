package tudbut.mod.client.ttc.utils;

public class ThreadManager { // Self-explanatory
    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }
    public static void run(String name, Runnable runnable) {
        new Thread(runnable, name).start();
    }
}
