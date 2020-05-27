package sh.chuu.port.mc.portchuu.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import sh.chuu.port.mc.portchuu.PortChuu;
import sh.chuu.port.mc.portchuu.TextTemplates;

import java.text.DecimalFormat;
import java.util.List;

public class CmdTps implements TabExecutor {
    private static final String RAM_PERM = "portchuu.command.tps.ram";
    private static final String RAM = "ram";
    private static final String BAR100 = "||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||";
    private static final String BAR40 = "||||||||||||||||||||||||||||||||||||||||";
    private final PortChuu plugin = PortChuu.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isConsole = sender instanceof ConsoleCommandSender;
        boolean canRam = sender.hasPermission(RAM_PERM);

        TextComponent tpsText = new TextComponent("TPS: ");
        tpsText.setColor(ChatColor.GOLD);

        if (canRam && (label.equalsIgnoreCase(RAM) || ((args.length != 0) && args[0].equalsIgnoreCase(RAM)))) {
            TextComponent[] tps = tps(isConsole ? "; " : "\n");
            TextComponent ramText = new TextComponent("RAM: [");
            TextComponent ramTextEnd = new TextComponent("] ");
            ramText.setColor(ChatColor.GOLD);
            ramTextEnd.setColor(ChatColor.GOLD);

            TextComponent[] ram = ram(isConsole ? BAR40 : BAR100);
            sender.sendMessage(new TextComponent[]{
                    tpsText,
                    tps[0],
                    tps[1],
                    tps[2],
                    ramText,
                    ram[0],
                    ramTextEnd,
                    ram[1]
            });
        } else {
            TextComponent[] tps = tps("");
            sender.sendMessage(new TextComponent[]{
                    tpsText,
                    tps[0],
                    tps[1],
                    tps[2]
            });
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase(RAM) || !sender.hasPermission(RAM_PERM) || args.length > 1 || !RAM.startsWith(args[0].toLowerCase()))
            return ImmutableList.of();
        return ImmutableList.of(RAM);
    }

    private TextComponent[] tps(String last) {
        DecimalFormat f = new DecimalFormat("#.0#");
        double[] tps = Bukkit.getTPS();

        TextComponent tps1m = new TextComponent(f.format(tps[0]) + ", ");
        TextComponent tps5m = new TextComponent(f.format(tps[1]) + ", ");
        TextComponent tps15m = new TextComponent(f.format(tps[2]) + last);
        tps1m.setColor(TextTemplates.colorTPS(tps[0]));
        tps5m.setColor(TextTemplates.colorTPS(tps[1]));
        tps15m.setColor(TextTemplates.colorTPS(tps[2]));
        tps1m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("1 minute")}));
        tps5m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("5 minutes")}));
        tps15m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("15 minutes")}));
        return new TextComponent[]{tps1m, tps5m, tps15m};
    }

    private TextComponent[] ram(String bar) {
        DecimalFormat f = new DecimalFormat("#.0");
        Runtime r = Runtime.getRuntime();
        r.gc();
        long allocated = r.totalMemory(); // anywhere between Xms and Xmx
        long free = r.freeMemory(); // free
        long max = r.maxMemory(); // Xmx
        long used = allocated - free;
        float pctUsed = ((float) used)/max;
        float pctAllocated = ((float) allocated)/max;
        float pctFree = 1 - pctUsed;
        ChatColor color = pctUsed < 0.85 ? ChatColor.GREEN : pctUsed < 0.96 ? ChatColor.YELLOW : ChatColor.RED;

        String maxHuman = TextTemplates.humanReadableBytes(max);
        String usedHuman = TextTemplates.humanReadableBytes(used);

        TextComponent rFree = new TextComponent(usedHuman + "/" + maxHuman);
        rFree.setColor(color);

        BaseComponent[] hover = {
                new TextComponent("Xmx: " + maxHuman
                        + "\nAllocated: " + TextTemplates.humanReadableBytes(allocated) + " (" + f.format(pctAllocated * 100) + "%)"
                        + "\nUsed: "),
                new TextComponent(usedHuman + " (" + f.format(pctUsed * 100) + "%)" + "\n"),
                new TextComponent("Free: "),
                new TextComponent(TextTemplates.humanReadableBytes(free) + " (" + f.format(pctFree * 100) + "%)")
        };

        hover[1].setColor(color);
        hover[3].setColor(color);

        int pt1 = (int) (pctUsed * bar.length());
        int pt2 = (int) (pctAllocated * bar.length());

        TextComponent r1 = new TextComponent(bar.substring(0, pt1));
        TextComponent r2 = new TextComponent(bar.substring(pt1, pt2));
        TextComponent r3 = new TextComponent(bar.substring(pt2));
        r1.setColor(color);
        r2.setColor(ChatColor.GRAY);
        r3.setColor(ChatColor.DARK_GRAY);
        r1.addExtra(r2);
        if (!r3.getText().isEmpty()) // can be empty if full Xmx is being utilized
            r1.addExtra(r3);
        r1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        return new TextComponent[]{r1, rFree};
    }
}
