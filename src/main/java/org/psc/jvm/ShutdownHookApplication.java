package org.psc.jvm;

public class ShutdownHookApplication {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ": shutting down");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("good bye!");
        }));
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> System.out.println(Thread.currentThread().getName() + ": oh no")));
    }
}
