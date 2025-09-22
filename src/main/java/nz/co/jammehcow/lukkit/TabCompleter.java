package nz.co.jammehcow.lukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    private static String[] subCommands = {"help", "plugins", "dev", "run"};
    private static String[] devSubCommands = {"reload", "unload", "pack", "unpack", "last-error", "errors"};

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        if (command.getName().startsWith("lukkit")) {
            if (args.length == 1) {
                return getFilteredCompletions(args[0], subCommands);
            } else if (args.length == 2) {
                String cmd = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);
                if (cmd.equalsIgnoreCase("dev")) {
                    List<String> plugins = new ArrayList<>();
                    LukkitContainer.getInstance().getLoadedPlugins().keySet().forEach(plugins::add);
                    String[] pluginArr = plugins.toArray(new String[0]);
                    if (args.length == 1) {
                        return getFilteredCompletions(args[0], devSubCommands);
                    } else if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("unload") || 
                            args[0].equalsIgnoreCase("pack") || args[0].equalsIgnoreCase("unpack")) {
                            return getFilteredCompletions(args[1], pluginArr);
                        }
                    }
                }
            }
        }
        return tabComplete;
    }

    private List<String> getFilteredCompletions(String arg, String[] subCommands) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        if (!arg.equals("")) {
            for (String s : subCommands) {
                if (s.startsWith(arg)) returnCompletions.add(s);
            }
            return returnCompletions;
        } else {
            return Arrays.asList(subCommands);
        }
    }
}
