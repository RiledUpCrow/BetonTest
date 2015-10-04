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
package pl.betoncraft.betontest.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.core.Test;

/**
 * Blocks specified commands while players are taking the test.
 *
 * @author Jakub Sapalski
 */
public class CommandListener implements Listener {
	
	private BetonTest plugin;

	public CommandListener(BetonTest plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Test test = plugin.getActiveTest(event.getPlayer());
		if (test == null) return;
		List<String> blocked = test.getBlockedCmds();
		if (blocked == null) return;
		String command = event.getMessage().substring(1);
		if (command == null) return;
		if (blocked.contains(command)) {
			event.setCancelled(true);
		}
	}
}
