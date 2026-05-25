package com.cortezromeo.clansplus.task;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.api.storage.IClanData;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.language.Messages;
import com.cortezromeo.clansplus.storage.PluginDataManager;
import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        for (String clanName : PluginDataManager.getClanDatabase().keySet()) {
            IClanData clanData = PluginDataManager.getClanDatabase(clanName);
            if (clanData == null) continue;

            double fee = Settings.CLAN_FUND_DAILY_FEE_AMOUNT;
            if (clanData.getFund() >= fee) {
                clanData.setFund(clanData.getFund() - fee);
                PluginDataManager.saveClanDatabaseToStorage(clanName, clanData);
                ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_FUND_DAILY_FEE
                        .replace("%fee%", String.format("%.2f", fee))
                        .replace("%fund%", String.format("%.2f", clanData.getFund())));
            } else {
                double remaining = clanData.getFund();
                clanData.setFund(0);
                PluginDataManager.saveClanDatabaseToStorage(clanName, clanData);
                ClanManager.alertClan(clanName, Messages.CLAN_BROADCAST_FUND_DAILY_FEE_INSUFFICIENT
                        .replace("%fee%", String.format("%.2f", fee))
                        .replace("%paid%", String.format("%.2f", remaining))
                        .replace("%fund%", "0.00"));
            }
        }
    }

    public WrappedTask getFundTask() {
        return fundTask;
    }
}
