package rx.bukkit.observable;

import org.bukkit.event.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2014 Ryan Michela
 */
public enum BukkitObservable { ;
    public static <EventT extends Event> Observable<EventT> fromBukkitEvent(Plugin plugin, Class<EventT> eventClass) {
        return fromBukkitEvent(plugin, eventClass, EventPriority.NORMAL, false);
    }

    public static <EventT extends Event> Observable<EventT> fromBukkitEvent(Plugin plugin, Class<EventT> eventClass, EventPriority priority) {
        return fromBukkitEvent(plugin, eventClass, priority, false);
    }

    public static <EventT extends Event> Observable<EventT> fromBukkitEvent(final Plugin plugin, final Class<EventT> eventClass, final EventPriority priority, final boolean ignoreCanceled) {
        return Observable.create(new Observable.OnSubscribe<EventT>() {
            @Override
            public void call(final Subscriber<? super EventT> subscriber) {
                EventExecutor eventExecutor = new EventExecutor() {
                    @Override
                    public void execute(Listener listener, Event event) throws EventException {
                        subscriber.onNext((EventT)event);
                    }
                };

                final Listener listener = new Listener() {};
                plugin.getServer().getPluginManager().registerEvent(eventClass, listener, priority, eventExecutor, plugin, ignoreCanceled);
                registerCompletionOnDisable(subscriber, plugin, listener);

                // Unregister the event handler on un-subscription
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        HandlerList.unregisterAll(listener);
                    }
                }));
            }
        });
    }

    private static Map<String, MultiplexingCommandExecutor> establishedExecutors = new HashMap<>();

    public static Observable<CommandEvent> fromBukkitCommand(final JavaPlugin plugin, final String command) {
        return Observable.create(new Observable.OnSubscribe<CommandEvent>() {
            @Override
            public void call(final Subscriber<? super CommandEvent> subscriber) {
                if (!establishedExecutors.containsKey(command)) {
                    MultiplexingCommandExecutor executor = new MultiplexingCommandExecutor();
                    plugin.getCommand(command).setExecutor(executor);
                    establishedExecutors.put(command, executor);
                }

                establishedExecutors.get(command).AddSubscriber(subscriber);
                registerCompletionOnDisable(subscriber, plugin, new Listener() {
                });
            }
        });
    }

    private static void registerCompletionOnDisable(final Subscriber subscriber, final Plugin plugin, Listener listener) {
        EventExecutor disableExecutor = new EventExecutor() {
            @Override
            public void execute(Listener listener, Event event) throws EventException {
                if (((PluginDisableEvent) event).getPlugin() == plugin) {
                    subscriber.onCompleted();
                }
            }
        };
        plugin.getServer().getPluginManager().registerEvent(PluginDisableEvent.class, listener, EventPriority.NORMAL, disableExecutor, plugin, false);
    }
}
