package tudbut.mod.client.ttc.utils;

public class ThreadManager {
    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }
}
