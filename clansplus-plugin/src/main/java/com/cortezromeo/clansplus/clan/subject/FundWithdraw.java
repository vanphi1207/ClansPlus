package com.cortezromeo.clansplus.clan.subject;

import com.cortezromeo.clansplus.ClansPlus;
import com.cortezromeo.clansplus.Settings;
import com.cortezromeo.clansplus.api.enums.Rank;
import com.cortezromeo.clansplus.api.enums.Subject;
import com.cortezromeo.clansplus.api.storage.IClanData;
import com.cortezromeo.clansplus.clan.ClanManager;
import com.cortezromeo.clansplus.clan.SubjectManager;
import com.cortezromeo.clansplus.language.Messages;
import com.cortezromeo.clansplus.storage.PluginDataManager;
import com.cortezromeo.clansplus.util.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class FundWithdraw extends SubjectManager {

    private final double amount;

    public FundWithdraw(Rank rank, Player player, String playerName, double amount) {
        super(rank, player, playerName, null, null);
        this.amount = amount;
    }

    @Override
    public boolean execute() {
        if (!Settings.CLAN_FUND_ENABLED) {
            MessageUtil.sendMessage(player, Messages.FEATURE_DISABLED);
            return false;
        }

        if (!isPlayerInClan()) {
            MessageUtil.sendMessage(player, Messages.MUST_BE_IN_CLAN);
            return false;
        }

        if (!Settings.CLAN_SETTING_PERMISSION_DEFAULT_FORCED)
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.FUND_WITHDRAW));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        if (amount <= 0) {
            MessageUtil.sendMessage(player, Messages.INVALID_NUMBER);
            return false;
        }

        if (amount < Settings.CLAN_FUND_MIN_WITHDRAW) {
            MessageUtil.sendMessage(player, Messages.FUND_MIN_WITHDRAW.replace("%min%", String.format("%.2f", Settings.CLAN_FUND_MIN_WITHDRAW)));
            return false;
        }

        Economy economy = ClansPlus.support.getVault();
        if (economy == null) {
            MessageUtil.sendMessage(player, Messages.FUND_VAULT_NOT_FOUND);
            return false;
        }

        IClanData clanData = getPlayerClanData();
        if (clanData.getFund() < amount) {
            MessageUtil.sendMessage(player, Messages.FUND_NOT_ENOUGH
                    .replace("%fund%", String.format("%.2f", clanData.getFund()))
                    .replace("%amount%", String.format("%.2f", amount)));
            return false;
        }

        clanData.setFund(clanData.getFund() - amount);
        economy.depositPlayer(player, amount);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);

        MessageUtil.sendMessage(player, Messages.FUND_WITHDRAW_SUCCESS
                .replace("%amount%", String.format("%.2f", amount))
                .replace("%fund%", String.format("%.2f", clanData.getFund())));

        ClanManager.alertClan(clanData.getName(), Messages.CLAN_BROADCAST_FUND_WITHDRAW
                .replace("%player%", playerName)
                .replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank()))
                .replace("%amount%", String.format("%.2f", amount))
                .replace("%fund%", String.format("%.2f", clanData.getFund())));

        return true;
    }
}
