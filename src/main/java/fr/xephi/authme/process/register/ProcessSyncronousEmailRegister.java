package fr.xephi.authme.process.register;

import me.muizers.Notifications.Notification;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.Utils;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.task.MessageTask;
import fr.xephi.authme.task.TimeoutTask;

public class ProcessSyncronousEmailRegister implements Runnable {

    protected Player player;
    private AuthMe plugin;
    private Messages m = Messages.getInstance();

    public ProcessSyncronousEmailRegister(Player player, AuthMe plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!Settings.getRegisteredGroup.isEmpty()) {
            Utils.getInstance().setGroup(player, Utils.groupType.REGISTERED);
        }
        m._(player, "vb_nonActiv");
        int time = Settings.getRegistrationTimeout * 20;
        int msgInterval = Settings.getWarnMessageInterval;
        if (time != 0) {
            Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(player).getTimeoutTaskId());
            int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new TimeoutTask(plugin, player), time);
            LimboCache.getInstance().getLimboPlayer(player).setTimeoutTaskId(id);
        }

        Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(player).getMessageTaskId());
        int nwMsg = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new MessageTask(plugin, player, m._("login_msg"), msgInterval));
        LimboCache.getInstance().getLimboPlayer(player).setMessageTaskId(nwMsg);
        player.saveData();
        if (!Settings.noConsoleSpam)
            ConsoleLogger.info(player.getName() + " registered " + plugin.getIP(player));
        if (plugin.notifications != null) {
            plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " has registered by email!"));
        }
    }

}
