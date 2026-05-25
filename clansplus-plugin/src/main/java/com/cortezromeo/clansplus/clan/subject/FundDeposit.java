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

public class FundDeposit extends SubjectManager {

    private final double amount;

    public FundDeposit(Rank rank, Player player, String playerName, double amount) {
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
            setRequiredRank(getPlayerClanData().getSubjectPermission().get(Subject.FUND_DEPOSIT));

        if (!isPlayerRankSatisfied()) {
            MessageUtil.sendMessage(player, Messages.REQUIRED_RANK.replace("%requiredRank%", ClanManager.getFormatRank(getRequiredRank())));
            return false;
        }

        if (amount <= 0) {
            MessageUtil.sendMessage(player, Messages.INVALID_NUMBER);
            return false;
        }

        if (amount < Settings.CLAN_FUND_MIN_DEPOSIT) {
            MessageUtil.sendMessage(player, Messages.FUND_MIN_DEPOSIT.replace("%min%", String.format("%.2f", Settings.CLAN_FUND_MIN_DEPOSIT)));
            return false;
        }

        Economy economy = ClansPlus.support.getVault();
        if (economy == null) {
            MessageUtil.sendMessage(player, Messages.FUND_VAULT_NOT_FOUND);
            return false;
        }

        if (!economy.has(player, amount)) {
            MessageUtil.sendMessage(player, Messages.NOT_ENOUGH_CURRENCY
                    .replace("%currencySymbol%", Messages.CURRENCY_DISPLAY_VAULT_SYMBOL)
                    .replace("%price%", String.format("%.2f", amount))
                    .replace("%currencyName%", Messages.CURRENCY_DISPLAY_VAULT_NAME));
            return false;
        }

        economy.withdrawPlayer(player, amount);

        IClanData clanData = getPlayerClanData();
        clanData.setFund(clanData.getFund() + amount);
        PluginDataManager.saveClanDatabaseToStorage(clanData.getName(), clanData);

        MessageUtil.sendMessage(player, Messages.FUND_DEPOSIT_SUCCESS
                .replace("%amount%", String.format("%.2f", amount))
                .replace("%fund%", String.format("%.2f", clanData.getFund())));

        ClanManager.alertClan(clanData.getName(), Messages.CLAN_BROADCAST_FUND_DEPOSIT
                .replace("%player%", playerName)
                .replace("%rank%", ClanManager.getFormatRank(PluginDataManager.getPlayerDatabase(playerName).getRank()))
                .replace("%amount%", String.format("%.2f", amount))
                .replace("%fund%", String.format("%.2f", clanData.getFund())));

        return true;
    }
}
