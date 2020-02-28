package sh.chuu.port.mc.portchuu;

import com.google.common.collect.ImmutableList;
import github.scarsz.discordsrv.DiscordSRV;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.chuu.port.mc.portchuu.commands.*;

import java.util.List;

public class PortChuu extends JavaPlugin {
    private static PortChuu instance = null;
    private NicknameManager nicknameManager = null;
    private LuckPerms lpAPI = null;
    private DiscordSRVHook discordSRVHook = null;

    public static PortChuu getInstance() {
        return PortChuu.instance;
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }

    public LuckPerms getLpAPI() {
        if (lpAPI != null)
            return lpAPI;

        try {
            Class.forName("net.luckperms.api.LuckPerms");
            RegisteredServiceProvider<LuckPerms> lpProvider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (lpProvider != null) this.lpAPI = lpProvider.getProvider();
        } catch (ClassNotFoundException e) {
            return null;
        }

        return lpAPI;
    }

    public boolean isDiscordSRVLoaded() {
        Plugin pl = getServer().getPluginManager().getPlugin("DiscordSRV");
        return pl != null && pl.isEnabled();
    }

    @Override
    public void onEnable() {
        PortChuu.instance = this;
        saveDefaultConfig();

        String chatFormat = getConfig().getBoolean("chat.reformat") ? getConfig().getString("chat.format") : null;
        PlayerEvents pe = new PlayerEvents(this, chatFormat);
        if (isDiscordSRVLoaded())
            DiscordSRV.api.subscribe(discordSRVHook = new DiscordSRVHook(pe));
        else
            getLogger().warning("DiscordSRV not enabled!");

        nicknameManager = new NicknameManager(this);
        getServer().getPluginManager().registerEvents(pe, this);

        PluginCommand cmdGamemode = getCommand("gamemode");
        PluginCommand cmdGraylist = getCommand("graylist");
        PluginCommand cmdKill = getCommand("kill");
        PluginCommand cmdNickname = getCommand("nickname");
        PluginCommand cmdPing = getCommand("ping");
        PluginCommand cmdInfo = getCommand("info");
        PluginCommand cmdTeleport = getCommand("teleport");

        cmdGamemode.setExecutor(new CmdGamemode());
        cmdGraylist.setExecutor(new CmdGraylist());
        cmdKill.setExecutor(new CmdKill());
        cmdNickname.setExecutor(new CmdNickname(nicknameManager));
        cmdPing.setExecutor(new CmdPing());
        cmdInfo.setExecutor(new CmdInfo());
        cmdTeleport.setExecutor(new CmdTeleport());

        getConfig();
        reloadConfig();
        saveConfig();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        nicknameManager.onDisable();
        if (isDiscordSRVLoaded())
            DiscordSRV.api.unsubscribe(discordSRVHook);
        PortChuu.instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TextComponent msg = new TextComponent("Port Chuu Server Plugin v");
        msg.addExtra(getDescription().getVersion());
        msg.addExtra("\nCoded with <3");
        msg.setColor(ChatColor.GRAY);
        sender.spigot().sendMessage(msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return ImmutableList.of();
    }

}
