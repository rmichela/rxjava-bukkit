package rx.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rx.Scheduler;
import rx.bukkit.scheduler.BukkitRxScheduler;
import rx.bukkit.task.TaskFactory;
import rx.plugins.RxJavaDefaultSchedulers;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;

import java.util.logging.Level;

/**
 * Copyright 2014 Ryan Michela
 */
public class RxJavaPlugin extends JavaPlugin {
    private final TaskFactory taskFactory = new TaskFactory(this);

    public static void initializeRx(final Plugin plugin) {
        // Register global error handler
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                plugin.getLogger().log(Level.SEVERE, "Unhandled exception in observable", e);
            }
        });
        // Register global schedulers
        RxJavaPlugins.getInstance().registerDefaultSchedulers(new RxJavaDefaultSchedulers() {
            @Override
            public Scheduler getComputationScheduler() {
                return BukkitRxScheduler.forPlugin(plugin, BukkitRxScheduler.ConcurrencyMode.SYNCHRONOUS);
            }

            @Override
            public Scheduler getIOScheduler() {
                return BukkitRxScheduler.forPlugin(plugin, BukkitRxScheduler.ConcurrencyMode.ASYNCHRONOUS);
            }

            @Override
            public Scheduler getNewThreadScheduler() {
                return BukkitRxScheduler.forPlugin(plugin, BukkitRxScheduler.ConcurrencyMode.ASYNCHRONOUS);
            }
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        initializeRx(this);
    }

    public TaskFactory getTaskFactory() {
        return taskFactory;
    }
}
