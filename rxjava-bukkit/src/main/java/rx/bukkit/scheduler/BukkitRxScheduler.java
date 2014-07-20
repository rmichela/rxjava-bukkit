package rx.bukkit.scheduler;

import org.bukkit.plugin.Plugin;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

/**
 * Copyright 2014 Ryan Michela
 */
public class BukkitRxScheduler extends Scheduler {
    private final Plugin plugin;

    public static BukkitRxScheduler forPlugin(Plugin plugin) {
        return new BukkitRxScheduler(plugin);
    }

    public BukkitRxScheduler(Plugin plugin) {
        this.plugin = plugin;
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

            final int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, scheduledAction, timeUnitToBukkitTicks(delayTime, timeUnit));
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
}
