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
package pl.betoncraft.betontest.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.exception.CategoryException;
import pl.betoncraft.betontest.exception.TestException;
import pl.betoncraft.betontest.utility.Saver;

/**
 * Represents a sinle test.
 *
 * @author Jakub Sapalski
 */
public class Test {
	
	private BetonTest plugin;
	private String name;
	private String messageStart;
	private String messageResume;
	private String messagePass;
	private String messageFail;
	private String messagePause;
	private String commandWin;
	private String commandFail;
	private String commandPause;
	private List<String> blockedCommands;
	private boolean teleportBack;
	private int maxMistakes;
	private MistakeAction onMistake;
	private ArrayList<Category> categories = new ArrayList<>();
	private HashMap<UUID, Category> players = new HashMap<>();
	private HashMap<UUID, Integer> chances = new HashMap<>();
	private ArrayList<UUID> paused = new ArrayList<>();
	
	/**
	 * Loads new test with given name and ConfigurationSection.
	 * 
	 * @param plugin
	 *            instance of BetonTest
	 * @param name
	 *            name of the test
	 * @param config
	 *            section containing the test
	 * @throws TestException
	 *             when configuration is incorrect
	 */
	public Test(BetonTest plugin, String name, ConfigurationSection config)
			throws TestException {
		this.plugin = plugin;
		this.name = name;
		messageStart = config.getString("messages.start");
		messageResume = config.getString("messages.resume");
		messagePass = config.getString("messages.pass");
		messageFail = config.getString("messages.fail");
		messagePause = config.getString("messages.pause");
		commandWin = config.getString("commands.pass");
		commandFail = config.getString("commands.fail");
		commandPause = config.getString("commands.pause");
		blockedCommands = config.getStringList("blocked_cmds");
		teleportBack = config.getBoolean("teleport_back");
		maxMistakes = config.getInt("max_mistakes", -1);
		if (maxMistakes < 0) 
			throw new TestException("max_mistakes must be more than 0");
		onMistake = MistakeAction.match(config.getString("on_mistake"));
		if (onMistake == null) 
			throw new TestException("incorrect on_mistake value");
		if (config.getConfigurationSection("categories") == null)
			throw new TestException("categories not defined");
		for (String key : config.getConfigurationSection("categories")
				.getKeys(false)) {
			try {
				categories.add(new Category(plugin, this, key,
						config.getConfigurationSection("categories." + key)));
			} catch (CategoryException e) {
				throw new TestException("error in '" + key + "' category: "
						+ e.getMessage());
			}
		}
		if (categories.isEmpty())
			throw new TestException("categories not defined");
		for (Category category : categories) {
			category.shuffle();
		}
	}
	
	/**
	 * Starts the test for the player.
	 * 
	 * @param player
	 */
	public void startTest(Player player) {
		if (plugin.getTest(player) != null) return;
		plugin.getData().set(player.getUniqueId() + ".current.name", name);
		plugin.getData().set(player.getUniqueId() + ".current.chances",
				maxMistakes);
		plugin.getData().set(player.getUniqueId() + ".location",
				player.getLocation().getX() + ";" + 
				player.getLocation().getY() + ";" + 
				player.getLocation().getZ() + ";" + 
				player.getLocation().getWorld().getName() + ";" + 
				player.getLocation().getYaw() + ";" +
				player.getLocation().getPitch());
		Category first = categories.get(0);
		players.put(player.getUniqueId(), first);
		chances.put(player.getUniqueId(), maxMistakes);
		if (messageStart != null) 
			player.sendMessage(messageStart.replace('&', '§'));
		first.addPlayer(player);
		new Saver(plugin);
	}
	
	/**
	 * Resumes the test for the player.
	 */
	public void resumeTest(Player player) {
		if (plugin.getActiveTest(player) != null) return;
		Category first = categories.get(0);
		plugin.getData().set(player.getUniqueId() + ".paused", null);
		paused.remove(player.getUniqueId());
		players.put(player.getUniqueId(), first);
		plugin.getData().set(player.getUniqueId() + ".current.name", name);
		plugin.getData().set(player.getUniqueId() + ".current.chances",
				chances.get(player.getUniqueId()));
		if (messageResume != null) 
			player.sendMessage(messageResume.replace('&', '§'));
		first.addPlayer(player);
		new Saver(plugin);
	}
	
	/**
	 * Loads a player using specified data.
	 * 
	 * @param player
	 *            the player to load
	 * @param chances
	 *            amount of chances left
	 * @param name
	 *            name of the category
	 * @param paused
	 *            is the test paused or not
	 */
	public void loadTest(Player player, int chances, String name,
			boolean paused) {
		this.chances.put(player.getUniqueId(), chances);
		if (paused) {
			this.paused.add(player.getUniqueId());
		} else {
			for (Category category : categories) {
				if (category.getName().equals(name)) {
					players.put(player.getUniqueId(), category);
					category.loadPlayer(player);
					return;
				}
			}
		}
	}
	
	/**
	 * Removes all playerdata from this test. This does not end it
	 * nor suspend it!
	 */
	public void unloadTest(Player player) {
		chances.remove(player.getUniqueId());
		paused.remove(player.getUniqueId());
		players.remove(player.getUniqueId());
	}
	
	/**
	 * Handles the correct answer for this player.
	 */
	public void correct(Player player) {
		Category current = players.get(player.getUniqueId());
		current.removePlayer(player);
		int index = categories.indexOf(current);
		if (index + 1 == categories.size()) {
			players.remove(player.getUniqueId());
			chances.remove(player.getUniqueId());
			pass(player);
		} else {
			Category next = categories.get(index + 1);
			players.put(player.getUniqueId(), next);
			next.addPlayer(player);
		}
		new Saver(plugin);
	}
	
	/**
	 * Handles the correct answer for this player.
	 */
	public void incorrect(Player player) {
		Category current = players.get(player.getUniqueId());
		current.removePlayer(player);
		int index = categories.indexOf(current);
		Integer chance = chances.get(player.getUniqueId());
		chance--;
		plugin.getData().set(player.getUniqueId() + ".current.chances", chance);
		if (chance <= 0) {
			players.remove(player.getUniqueId());
			chances.remove(player.getUniqueId());
			fail(player);
		} else {
			chances.put(player.getUniqueId(), chance);
			switch (onMistake) {
			case NEXT:
				Category next = categories.get(index + 1);
				players.put(player.getUniqueId(), next);
				next.addPlayer(player);
				break;
			case NOTHING:
				current.addPlayer(player);
				break;
			case RESTART:
				Category first = categories.get(0);
				players.put(player.getUniqueId(), first);
				first.addPlayer(player);
				break;
			case PAUSE:
				players.remove(player.getUniqueId());
				paused.add(player.getUniqueId());
				pause(player);
				break;
			}
		}
		new Saver(plugin);
	}
	
	/**
	 * Passes the test for the player.
	 */
	private void pass(Player player) {
		if (messagePass != null) 
			player.sendMessage(messagePass.replace('&', '§'));
		if (commandWin != null)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandWin
				.replace("%player%", player.getName()));
		plugin.getData().set(player.getUniqueId() + ".current", null);
		List<String> newList = plugin.getData().getStringList(
				player.getUniqueId() + ".passed");
		newList.add(name);
		plugin.getData().set(player.getUniqueId() + ".passed", newList);
		plugin.logTest(player, this, true);
		if (teleportBack) player.teleport(getLocation(player));
	}
	
	/**
	 * Fails the test for the player.
	 */
	private void fail(Player player) {
		if (messageFail != null) 
			player.sendMessage(messageFail.replace('&', '§'));
		if (commandFail != null)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandFail
				.replace("%player%", player.getName()));
		plugin.getData().set(player.getUniqueId() + ".current", null);
		List<String> newList = plugin.getData().getStringList(
				player.getUniqueId() + ".failed");
		newList.add(name);
		plugin.getData().set(player.getUniqueId() + ".failed", newList);
		plugin.logTest(player, this, false);
		if (teleportBack) player.teleport(getLocation(player));
	}
	
	/**
	 * Pauses the test for the player.
	 */
	private void pause(Player player) {
		if (messagePause != null) 
			player.sendMessage(messagePause.replace('&', '§'));
		if (commandPause != null)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandPause
				.replace("%player%", player.getName()));
		plugin.getData().set(player.getUniqueId() + ".current", null);
		plugin.getData().set(player.getUniqueId() + ".paused.name", name);
		plugin.getData().set(player.getUniqueId() + ".paused.chances",
				chances.get(player.getUniqueId()));
	}
	
	/**
	 * @return the location at which the player was when he started the test
	 */
	public Location getLocation(Player player) {
		String[] parts = plugin.getData().getString(player.getUniqueId() +
				".location").split(";");
		plugin.getData().set(player.getUniqueId() + ".location", null);
		return new Location(
				Bukkit.getWorld(parts[3]),
				Double.parseDouble(parts[0]),
				Double.parseDouble(parts[1]),
				Double.parseDouble(parts[2]),
				Float.parseFloat(parts[4]),
				Float.parseFloat(parts[5])
				);
	}
	
	/**
	 * @return the category for this player
	 */
	public Category getPlayerCategory(Player player) {
		return players.get(player.getUniqueId());
	}
	
	/**
	 * @return if the player has this test paused
	 */
	public boolean isPaused(Player player) {
		return paused.contains(player.getUniqueId());
	}

	/**
	 * @return the name of the test
	 */
	public String getName() {
		return name;
	}
	
	public List<String> getBlockedCmds() {
		return blockedCommands;
	}
	
	/**
	 * @return true if the test will teleport back the player after he finishes
	 *         it
	 */
	public boolean isTeleportingBack() {
		return teleportBack;
	}

	private enum MistakeAction {
		NEXT, NOTHING, RESTART, PAUSE;
		
		public static MistakeAction match(String string) {
			if (string == null) return null;
			MistakeAction action = null;
			try {
				action = valueOf(string.toUpperCase());
			} catch (IllegalArgumentException e) {}
			return action;
		}
	}
	
}
