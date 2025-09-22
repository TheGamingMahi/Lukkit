package nz.co.jammehcow.lukkit;

import nz.co.jammehcow.lukkit.environment.plugin.InternalLuaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Map;

public class LukkitCommandExecutor implements CommandExecutor {
    private final LukkitContainer container;
    public LukkitCommandExecutor(LukkitContainer container) { this.container = container; }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sender.sendMessage(getHelpMessage()); return true; }
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "plugins": listPlugins(sender); return true;
            case "help": sender.sendMessage(getHelpMessage()); return true;
            case "dev": 
                if (args.length > 1 && args[1].equalsIgnoreCase("plugins")) { listPlugins(sender); return true; }
                sender.sendMessage(getDevHelpMessage()); return true;
            default: sender.sendMessage(getHelpMessage()); return true;
        }
    }
    
    private void listPlugins(CommandSender sender) {
        Map<String, InternalLuaPlugin> plugins = container.getLoadedPlugins();
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN).append("Lukkit Lua Plugins (").append(plugins.size()).append("):\n");
        for (InternalLuaPlugin plugin : plugins.values()) {
            sb.append(ChatColor.YELLOW).append("  - ").append(plugin.getName())
              .append(ChatColor.GRAY).append(" v").append(plugin.getVersion());
            if (!plugin.isEnabled()) sb.append(ChatColor.RED).append(" (Disabled)");
            sb.append("\n");
        }
        sender.sendMessage(sb.toString());
    }
    
    private String getHelpMessage() {
        return ChatColor.GREEN + "Lukkit Commands:\n" +
               ChatColor.YELLOW + "  /lukkit plugins " + ChatColor.GRAY + "- List loaded Lua plugins\n" +
               ChatColor.YELLOW + "  /lukkit help " + ChatColor.GRAY + "- Show this message\n" +
               ChatColor.YELLOW + "  /lukkit dev " + ChatColor.GRAY + "- Developer commands";
    }
    
    private String getDevHelpMessage() {
        return ChatColor.GREEN + "Lukkit Dev Commands:\n" +
               ChatColor.YELLOW + "  /lukkit dev plugins " + ChatColor.GRAY + "- List Lua plugins\n" +
               ChatColor.YELLOW + "  /lukkit dev help " + ChatColor.GRAY + "- Show this message";
    }
}
