package TestPlugin;

import RxBukkt.BukkitObservable;
import RxBukkt.BukkitRxScheduler;
import RxBukkt.CommandEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2014 Ryan Michela
 */
public class Plugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Observable<PlayerInteractEvent> eveObs = BukkitObservable.fromBukkitEvent(this, PlayerInteractEvent.class);

        eveObs
            .buffer(1, TimeUnit.SECONDS, BukkitRxScheduler.forPlugin(this))
            .filter(new Func1<List<PlayerInteractEvent>, Boolean>() {
                @Override
                public Boolean call(List<PlayerInteractEvent> playerInteractEvents) {
                    return playerInteractEvents.size() > 2;
                }
            })
            .map(new Func1<List<PlayerInteractEvent>, String>() {
                @Override
                public String call(List<PlayerInteractEvent> playerInteractEvents) {
                    return playerInteractEvents.size() + " clicks";
                }
            })
//            .observeOn(BukkitRxScheduler.forPlugin(this))
            .subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    getLogger().info(Thread.currentThread().getId() + " " + s);
                }
            });

        eveObs
            .subscribe(new Action1<PlayerInteractEvent>() {
                @Override
                public void call(PlayerInteractEvent playerInteractEvent) {
                    getLogger().info(Thread.currentThread().getId() + "");
                }
            });

        Observable<CommandEvent> cmdObs = BukkitObservable.fromBukkitCommand(this, "cmd");
        cmdObs.subscribe(new Action1<CommandEvent>() {
            @Override
            public void call(CommandEvent commandEvent) {
                getLogger().info(commandEvent.getCommand() + " " + commandEvent.getArgs().length);
            }
        });

        cmdObs.subscribe(new Action1<CommandEvent>() {
            @Override
            public void call(CommandEvent commandEvent) {
                getLogger().info(commandEvent.isCancelled() + "");
            }
        });
    }
}
