package tudbut.mod.client.ttc.utils;

public class ThreadManager { // Self-explanatory
    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }
}
