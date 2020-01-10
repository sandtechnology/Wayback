package com.ilummc.wayback;

import com.ilummc.wayback.backups.FileBackup;
import com.ilummc.wayback.backups.SqlBackup;
import com.ilummc.wayback.cmd.CommandRegistry;
import com.ilummc.wayback.compress.ZipCompressor;
import com.ilummc.wayback.policy.AbandonPolicy;
import com.ilummc.wayback.policy.CleanLatestPolicy;
import com.ilummc.wayback.policy.CleanOldestPolicy;
import com.ilummc.wayback.policy.RetryPolicy;
import com.ilummc.wayback.schedules.PreloadSchedule;
import com.ilummc.wayback.storage.FtpStorage;
import com.ilummc.wayback.storage.LocalStorage;
import com.ilummc.wayback.tasks.RollbackTask;
import com.ilummc.wayback.tasks.TransferTask;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import static com.ilummc.wayback.Wayback.instance;

final class DelegatedWayback {

    static void onEnable() {
        TLocale.sendToConsole("LOGO", instance().getDescription().getVersion());
        registerSerializable();
        WaybackConf.init();
        CommandRegistry.init();
        CommandRegistry.register(new WaybackCommand());
        Environment.check();
        Stats.init();

        new Metrics(instance());
        if (instance().getConfig().isSet("checkUpdate")) {
            instance().getConfig().set("checkUpdate", false);
            instance().saveConfig();
        }
        if (instance().getConfig().getBoolean("checkUpdate")) {
            WaybackUpdater.start();
        }
    }

    private static void registerSerializable() {
        ConfigurationSerialization.registerClass(FileBackup.class, "File");
        ConfigurationSerialization.registerClass(FileBackup.class, "FileBackup");
        ConfigurationSerialization.registerClass(SqlBackup.class, "SQL");
        ConfigurationSerialization.registerClass(SqlBackup.class, "SqlBackup");
        ConfigurationSerialization.registerClass(ZipCompressor.class, "zip");
        ConfigurationSerialization.registerClass(FtpStorage.class, "FtpStorage");
        ConfigurationSerialization.registerClass(LocalStorage.class, "LocalStorage");
        ConfigurationSerialization.registerClass(AbandonPolicy.class, "Abandon");
        ConfigurationSerialization.registerClass(CleanOldestPolicy.class, "CleanOldest");
        ConfigurationSerialization.registerClass(CleanLatestPolicy.class, "CleanLatest");
        ConfigurationSerialization.registerClass(RetryPolicy.class, "Retry");
        ConfigurationSerialization.registerClass(TransferTask.class, "Transfer");
        ConfigurationSerialization.registerClass(PreloadSchedule.NormalPreload.class, "Instant");
        ConfigurationSerialization.registerClass(PreloadSchedule.PeriodPreload.class, "Period");
        ConfigurationSerialization.registerClass(PreloadSchedule.DelayedPreload.class, "Delayed");
        ConfigurationSerialization.registerClass(RollbackTask.class, "Rollback");
    }

    static void onDisable() {
        TLocale.sendToConsole("LOGO", instance().getDescription().getVersion());
        try {
            Wayback.getSchedules().shutdown();
        } catch (InterruptedException e) {
            TLocale.Logger.error("TERMINATE_ERROR");
        }
    }

}
