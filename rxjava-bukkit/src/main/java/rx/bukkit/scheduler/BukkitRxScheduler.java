package rx.bukkit.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2014 Ryan Michela
 */
public class BukkitRxScheduler extends Scheduler implements Executor {
    public static enum ConcurrencyMode {SYNCHRONOUS, ASYNCHRONOUS}

    private final Plugin plugin;
    private final ConcurrencyMode concurrencyMode;

    public static BukkitRxScheduler forPlugin(Plugin plugin, ConcurrencyMode concurrencyMode) {
        return new BukkitRxScheduler(plugin, concurrencyMode);
    }

    public static BukkitRxScheduler forPlugin(Plugin plugin) {
        return new BukkitRxScheduler(plugin, ConcurrencyMode.SYNCHRONOUS);
    }

    public BukkitRxScheduler(Plugin plugin, ConcurrencyMode concurrencyMode) {
        this.plugin = plugin;
        this.concurrencyMode = concurrencyMode;
    }

    @Override
    public void execute(Runnable command) {
        scheduleOnBukkitScheduler(plugin, command, 0, concurrencyMode);
    }

    @Override
    public Worker createWorker() {
        return new BukkitRxSchedulerWorker();
    }

    private class BukkitRxSchedulerWorker extends Worker {
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        @Override
        public Subscription schedule(Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit timeUnit) {
            final ScheduledAction scheduledAction = new ScheduledAction(action);
            scheduledAction.addParent(compositeSubscription);
            compositeSubscription.add(scheduledAction);

            final int taskId = BukkitRxScheduler.scheduleOnBukkitScheduler(plugin, scheduledAction, timeUnitToBukkitTicks(delayTime, timeUnit), concurrencyMode);
            scheduledAction.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    plugin.getServer().getScheduler().cancelTask(taskId);
                }
            }));

            return scheduledAction;
        }

        @Override
        public void unsubscribe() {
            compositeSubscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return compositeSubscription.isUnsubscribed();
        }

        private long timeUnitToBukkitTicks(long delayTime, TimeUnit timeUnit) {
            return Math.round(timeUnit.toMillis(delayTime) * 0.02);
        }
    }

    private static int scheduleOnBukkitScheduler(Plugin plugin, Runnable command, long ticks, ConcurrencyMode concurrencyMode) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (concurrencyMode == ConcurrencyMode.SYNCHRONOUS) {
            return scheduler.scheduleSyncDelayedTask(plugin, command, ticks);
        } else {
            return scheduler.scheduleAsyncDelayedTask(plugin, command, ticks);
        }
    }
}
