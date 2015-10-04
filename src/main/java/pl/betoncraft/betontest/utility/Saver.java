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
package pl.betoncraft.betontest.utility;

import org.bukkit.scheduler.BukkitRunnable;

import pl.betoncraft.betontest.BetonTest;

/**
 * Saves the data file asynchronously.
 *
 * @author Jakub Sapalski
 */
public class Saver extends BukkitRunnable {
	
	private BetonTest plugin;
	
	public Saver(BetonTest plugin) {
		this.plugin = plugin;
		runTaskAsynchronously(plugin);
	}

	@Override
	public void run() {
		plugin.saveData();
	}

}
