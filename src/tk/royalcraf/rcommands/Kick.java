package tk.royalcraf.rcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.royalcraf.royalcommands.RoyalCommands;

public class Kick implements CommandExecutor {

	RoyalCommands plugin;

	public Kick(RoyalCommands plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("kick")) {
			if (!plugin.isAuthorized(cs, "rcmds.kick")) {
				cs.sendMessage(ChatColor.RED
						+ "You don't have permission for that!");
				plugin.log.warning("[RoyalCommands] " + cs.getName()
						+ " was denied access to the command!");
				return true;
			}
			if (args.length < 1) {
				return false;
			}
			Player t = plugin.getServer().getPlayer(args[0]);
			if (t == null) {
				cs.sendMessage(ChatColor.RED + "That player is not online!");
				return true;
			}
			if (plugin.isAuthorized(t, "rcmds.exempt.kick")) {
				cs.sendMessage(ChatColor.RED + "You cannot kick that player!");
				return true;
			}
			if (args.length == 1) {
				plugin.getServer().broadcast(
						ChatColor.RED + "The player " + ChatColor.GRAY
								+ t.getName() + ChatColor.RED
								+ " has been kicked for " + ChatColor.GRAY
								+ plugin.kickMessage + ChatColor.RED + " by "
								+ ChatColor.GRAY + cs.getName() + ChatColor.RED
								+ ".", "rcmds.see.kick");
				t.kickPlayer(plugin.kickMessage);
				return true;
			} else if (args.length > 1) {
				String kickMessage = plugin.getFinalArg(args, 1).replaceAll(
						"(&([a-f0-9]))", "\u00A7$2");
				plugin.getServer().broadcast(
						ChatColor.RED + "The player " + ChatColor.GRAY
								+ t.getName() + ChatColor.RED
								+ " has been kicked for " + ChatColor.GRAY
								+ kickMessage + ChatColor.RED + " by "
								+ ChatColor.GRAY + cs.getName() + ChatColor.RED
								+ ".", "rcmds.see.kick");
				t.kickPlayer(kickMessage);
				return true;
			}
		}
		return false;
	}
}