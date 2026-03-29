package com.cortezromeo.clansplus.listener;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.clan.EventManager;
import com.cortezromeo.clansplus.storage.PluginDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

public class PlayerQuitListener implements Listener {

    public PlayerQuitListener() {
        Bukkit.getPluginManager().registerEvents(this, ClansPlus.plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PluginDataManager.getPlayerDatabase(player.getName()).setLastActivated(new Date().getTime());
        EventManager.getWarEvent().removeBossBar(player);

        ChatListenerHandler.createClan.remove(player);
        ChatListenerHandler.setCustomName.remove(player);
        ChatListenerHandler.setMessage.remove(player);
        ClanManager.getPlayerUsingClanChat().remove(player);
        ClanManager.getPlayerTogglingPvP().remove(player);
        SignChangeListener.removeSearchPlayerQuery(player);
    }

}
