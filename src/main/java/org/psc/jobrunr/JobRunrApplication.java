package org.psc.jobrunr;

import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.storage.InMemoryStorageProvider;

import java.time.Duration;

public class JobRunrApplication {

    public static void main(String[] args) {
        JobRunr.configure()
                .useStorageProvider(new InMemoryStorageProvider())
                .useBackgroundJobServer()
                .initialize();
        BackgroundJob.scheduleRecurrently(Duration.ofSeconds(10L), () -> System.out.println("Hello!"));
        while (true) {
        }
    }
}
