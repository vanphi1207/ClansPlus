package com.cortezromeo.clansplus.task;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.api.storage.IClanData;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.language.Messages;
import com.cortezromeo.clansplus.storage.PluginDataManager;
import com.cortezromeo.clansplus.util.MessageUtil;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FundTask implements Runnable {

    private WrappedTask fundTask;

    public FundTask() {
        this.fundTask = ClansPlus.support.getFoliaLib().getScheduler().runTimer(this, 1, 20L);
    }

    @Override
    public void run() {
        if (!Settings.CLAN_FUND_ENABLED) return;
        if (!Settings.CLAN_FUND_DAILY_FEE_ENABLED) return;

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String strDate = dateFormat.format(date);

        if (!strDate.equals(Settings.CLAN_FUND_DAILY_FEE_TIME)) return;

        List<String> clanNames = new ArrayList<>(PluginDataManager.getClanDatabase().keySet());

        for (String clanName : clanNames) {
            IClanData clanData = PluginDataManager.getClanDatabase(clanName);
            if (clanData == null) continue;

            double fee = Settings.CLAN_FUND_DAILY_FEE_AMOUNT;
            if (clanData.getFund() >= fee) {
                clanData.setFund(clanData.getFund() - fee);
                clanData.setMissedFeeCount(0);
                PluginDataManager.saveClanDatabaseToStorage(clanName, clanData);
                ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_FUND_DAILY_FEE
                        .replace("%fee%", String.format("%.2f", fee))
                        .replace("%fund%", String.format("%.2f", clanData.getFund())));
            } else {
                double remaining = clanData.getFund();
                int missed = clanData.getMissedFeeCount() + 1;
                clanData.setMissedFeeCount(missed);
                PluginDataManager.saveClanDatabaseToStorage(clanName, clanData);
                ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_FUND_DAILY_FEE_INSUFFICIENT
                        .replace("%fee%", String.format("%.2f", fee))
                        .replace("%paid%", String.format("%.2f", remaining))
                        .replace("%missed%", String.valueOf(missed))
                        .replace("%maxMissed%", String.valueOf(Settings.CLAN_FUND_DAILY_FEE_MAX_MISSED))
                        .replace("%fund%", String.format("%.2f", remaining)));

                if (missed >= Settings.CLAN_FUND_DAILY_FEE_MAX_MISSED) {
                    List<String> members = new ArrayList<>(clanData.getMembers());
                    String disbandMsg = Messages.CLAN_BROADCAST_FUND_DAILY_FEE_DISBAND
                            .replace("%clan%", clanName)
                            .replace("%missed%", String.valueOf(missed));
                    for (String memberName : members)
                        MessageUtil.sendMessage(Bukkit.getPlayer(memberName), disbandMsg);

                    PluginDataManager.deleteClanData(clanName);

                    if (Messages.SERVER_BROADCAST_FUND_DAILY_FEE_DISBAND != null && !Messages.SERVER_BROADCAST_FUND_DAILY_FEE_DISBAND.isEmpty()) {
                        String serverMsg = Messages.SERVER_BROADCAST_FUND_DAILY_FEE_DISBAND
                                .replace("%clan%", clanName)
                                .replace("%missed%", String.valueOf(missed));
                        Bukkit.getOnlinePlayers().forEach(p -> MessageUtil.sendMessage(p, serverMsg));
                    }
                }
            }
        }
    }

    public WrappedTask getFundTask() {
        return fundTask;
    }
}
