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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.core.Test;

/**
 * The command for players to start the test.
 *
 * @author Jakub Sapalski
 */
public class StartCommand implements CommandExecutor {
	
	private BetonTest plugin;
	
	public StartCommand(BetonTest plugin) {
		this.plugin = plugin;
		plugin.getCommand("starttest").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("starttest")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use it!");
				return true;
			}
			if (args.length < 1) {
				sender.sendMessage("§cSpecify test name.");
				return true;
			}
			Test test = plugin.getTests().get(args[0]);
			if (test == null) {
				sender.sendMessage("§cTest does not exist.");
				return true;
			}
			if (!sender.hasPermission("betontest.test." + test.getName())) {
				sender.sendMessage("§cYou don't have permission to start this test!");
				return true;
			}
			Player player = (Player) sender;
			if (plugin.getActiveTest(player) != null) {
				sender.sendMessage("§cYou are taking the test right now.");
				return true;
			}
			if (test.isPaused(player)) {
				test.resumeTest(player);
			} else {
				test.startTest(player);
			}
			return true;
		}
		return false;
	}

}
