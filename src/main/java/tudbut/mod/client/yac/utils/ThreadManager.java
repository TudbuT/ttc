package tudbut.mod.client.yac.utils;

public class ThreadManager {
    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }
}
