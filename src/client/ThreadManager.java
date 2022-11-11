package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreadManager {

    private final List<Thread> threads;

    public ThreadManager() {
        this.threads = new ArrayList<>();
    }

    public void addThreads(Thread... threads) {
        this.threads.addAll(Arrays.asList(threads));
    }

    public void stopThreads() {
        for (Thread t : threads) {
            if (t.isAlive()) {
                try { t.interrupt(); }
                catch (Exception e) {/*ignore on purpose*/}
            }
        }
    }

}
