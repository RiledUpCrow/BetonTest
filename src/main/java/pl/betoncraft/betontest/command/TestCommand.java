/**
 * BetonTest - quiz plugin for Bukkit
 * Copyright (C) 2015  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betontest.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.core.Test;
import pl.betoncraft.betontest.utility.Saver;

/**
 * Main administrative command.
 *
 * @author Jakub Sapalski
 */
public class TestCommand implements CommandExecutor {
	
	private BetonTest plugin;
	
	public TestCommand(BetonTest plugin) {
		this.plugin = plugin;
		plugin.getCommand("test").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("test")) {
			if (args.length == 0) {
				sender.sendMessage("§bAvailable options:");
				sender.sendMessage("§3/" + label + " reload§e - reloads the configuration.");
				sender.sendMessage("§3/" + label + " start <test> <player>§e - starts or resumes the test for the player.");
				sender.sendMessage("§3/" + label + " purge <player>§e - removes all data about the player from the data file.");
				return true;
			}
			switch (args[0]) {
			case "reload":
				plugin.reloadConfig();
				plugin.reloadData();
				plugin.load();
				sender.sendMessage("§2Reloaded!");
				return true;
			case "start":
				if (args.length < 3) {
					sender.sendMessage("§cNot enough arguments. Type §e/" + label + "§c for help.");
					return true;
				}
				@SuppressWarnings("deprecation")
				Player player = Bukkit.getPlayer(args[2]);
				if (player == null) {
					sender.sendMessage("§cThe player must be online!");
					return true;
				}
				Test test = plugin.getTests().get(args[1]);
				if (test == null) {
					sender.sendMessage("§cTest does not exist!");
					return true;
				}
				if (plugin.getActiveTest(player) != null) {
					sender.sendMessage("§cThis player has a test right now!");
					return true;
				}
				Test paused = plugin.getPausedTest(player);
				if (paused != null) {
					if (paused.equals(test)) {
						test.resumeTest(player);
					} else {
						sender.sendMessage("§cThis player has some paused test!");
						return true;
					}
				} else {
					test.startTest(player);
				}
				sender.sendMessage("§2Test started for player!");
				return true;
			case "purge":
				if (args.length < 2) {
					sender.sendMessage("§cNot enough arguments. Type §e/" + label + "§c for help.");
					return true;
				}
				@SuppressWarnings("deprecation")
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
				if (offlinePlayer == null) {
					sender.sendMessage("§cThe player does not exist!");
					return true;
				}
				boolean notify = plugin.getActiveTest((Player) offlinePlayer) != null;
				Test test2 = plugin.getTest((Player) offlinePlayer);
				if (test2 != null) {
					if (notify && test2.isTeleportingBack() && offlinePlayer.isOnline()) {
						((Player) offlinePlayer).teleport(test2.getLocation((Player) offlinePlayer));
						notify = false;
					}
					test2.unloadTest((Player) offlinePlayer);
				}
				plugin.getData().set(offlinePlayer.getUniqueId().toString(), null);
				new Saver(plugin);
				sender.sendMessage("§2Player purged!" + ((notify) ? " You need to teleport the player out of the test he was taking!" : ""));
				return true;
			default:
				sender.sendMessage("§cUnknown argument. Type §e/" + label + "§c for help.");
				return true;
			}
		}
		return false;
	}

}
