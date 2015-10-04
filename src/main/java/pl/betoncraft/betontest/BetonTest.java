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
package pl.betoncraft.betontest;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betontest.betonquest.ActiveCondition;
import pl.betoncraft.betontest.betonquest.FailedCondition;
import pl.betoncraft.betontest.betonquest.PassedCondition;
import pl.betoncraft.betontest.betonquest.PausedCondition;
import pl.betoncraft.betontest.betonquest.StartEvent;
import pl.betoncraft.betontest.command.StartCommand;
import pl.betoncraft.betontest.command.TestCommand;
import pl.betoncraft.betontest.core.Answer;
import pl.betoncraft.betontest.core.Question;
import pl.betoncraft.betontest.core.Test;
import pl.betoncraft.betontest.database.Database;
import pl.betoncraft.betontest.exception.TestException;
import pl.betoncraft.betontest.listener.AnswerListener;
import pl.betoncraft.betontest.listener.CommandListener;
import pl.betoncraft.betontest.listener.JoinQuitListener;
import pl.betoncraft.betontest.utility.Metrics;

/**
 * Main class.
 * 
 * @author Jakub Sapalski
 */
public class BetonTest extends JavaPlugin {
	
	private HashMap<String, Test> tests = new HashMap<>();
	private FileConfiguration data;
	private Database database;
	private BukkitRunnable keeper;
	private boolean databaseUsed = false;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		// handle the data
		File file = new File(getDataFolder(), "data.yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = YamlConfiguration.loadConfiguration(file);
		load();
		this.database = new Database(getConfig().getString("mysql.host"),
				getConfig().getString("mysql.port"), getConfig().getString(
				"mysql.base"), getConfig().getString("mysql.user"),
				getConfig().getString("mysql.pass"));
		// try to connect to MySQL
		if (database.getConnection() != null) {
			// create tables
			database.createTables();
			// keep the database connected
			keeper = new BukkitRunnable() {
				@Override
				public void run() {
					try {
						database.getConnection().prepareStatement("SELECT 1;")
								.executeQuery();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			};
			keeper.runTaskTimerAsynchronously(this, 20*60, 20*60);
			databaseUsed = true;
			getLogger().info("MySQL connected.");
		}
		// register BetonQuest events
		if (Bukkit.getPluginManager().isPluginEnabled("BetonQuest")) {
			BetonQuest.getInstance().registerEvents("teststart", StartEvent.class);
			BetonQuest.getInstance().registerConditions("testactive", ActiveCondition.class);
			BetonQuest.getInstance().registerConditions("testpaused", PausedCondition.class);
			BetonQuest.getInstance().registerConditions("testpassed", PassedCondition.class);
			BetonQuest.getInstance().registerConditions("testfailed", FailedCondition.class);
		}
		// start listeners
		new AnswerListener(this);
		new JoinQuitListener(this);
		new CommandListener(this);
		// register commands
		new TestCommand(this);
		new StartCommand(this);
		// start metrics
		try {
			new Metrics(this).start();
		} catch (IOException e) {}
	}

	/**
	 * Loads all data from the configuration.
	 */
	public void load() {
		// unload data for online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			unloadPlayer(player);
		}
		// clear the tests
		tests.clear();
		// load all tests
		ConfigurationSection section = getConfig().getConfigurationSection(
				"tests");
		if (section != null) {
			for (String test : section.getKeys(false)) {
				try {
					tests.put(test, new Test(this, test, getConfig()
							.getConfigurationSection("tests." + test)));
				} catch (TestException e) {
					getLogger().severe("Error in '" + test + "' test: " + 
							e.getMessage());
				}
			}
		}
		// load data for online players
		for (Player player : Bukkit.getOnlinePlayers()) {
			loadPlayer(player);
		}
	}
	
	@Override
	public void onDisable() {
		if (databaseUsed) {
			keeper.cancel();
			database.closeConnection();
		}
	}
	
	/**
	 * @return the HashMap of tests and their names
	 */
	public HashMap<String, Test> getTests() {
		return tests;
	}
	
	/**
	 * @return the data FileConfiguration
	 */
	public FileConfiguration getData() {
		return data;
	}
	
	/**
	 * Loads the player from the data file.
	 */
	public void loadPlayer(Player player) {
		String uuid = player.getUniqueId().toString();
		ConfigurationSection pd = data.getConfigurationSection(uuid);
		if (pd == null) return;
		ConfigurationSection current = pd.getConfigurationSection("current");
		ConfigurationSection paused = pd.getConfigurationSection("paused");
		if (current != null) {
			Test test = tests.get(current.getString("name"));
			if (test == null) {
				pd.set("current", null);
			} else {
				test.loadTest(player, current.getInt("chances"),
					current.getString("category"), false);
			}
		} else if (paused != null) {
			Test test = tests.get(paused.getString("name"));
			if (test == null) {
				pd.set("paused", null);
			} else {
				test.loadTest(player, paused.getInt("chances"),
					paused.getString("category"), false);
			}
		}
	}
	
	/**
	 * Removes the player from data model without failing, passing or pausing.
	 */
	public void unloadPlayer(Player player) {
		Test test = getTest(player);
		if (test == null) return;
		test.unloadTest(player);
	}
	
	/**
	 * @return the active test for this player
	 */
	public Test getActiveTest(Player player) {
		for (Test test : tests.values()) {
			if (test.getPlayerCategory(player) != null) return test;
		}
		return null;
	}
	
	/**
	 * @return the paused test for this player
	 */
	public Test getPausedTest(Player player) {
		for (Test test : tests.values()) {
			if (test.isPaused(player)) return test;
		}
		return null;
	}
	
	/**
	 * @return the active or paused test for this player
	 */
	public Test getTest(Player player) {
		Test test = getActiveTest(player);
		if (test == null) test = getPausedTest(player);
		return test;
	}
	
	/**
	 * Saves the data file.
	 */
	public synchronized void saveData() {
		try {
			data.save(new File(getDataFolder(), "data.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reloads the data.
	 */
	public void reloadData() {
		data = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "data.yml"));
	}
	
	/**
	 * Logs passing/failing the test to the database.
	 * 
	 * @param player
	 *            player who passed/failed a test
	 * @param test
	 *            test which has been passed/failed
	 * @param result
	 *            true if passed, false if failed
	 */
	public void logTest(final Player player, final Test test,
			final boolean result) {
		if (!databaseUsed) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					PreparedStatement stmt = database.getConnection()
							.prepareStatement("INSERT INTO tests (player, test,"
							+ " passed) VALUES (?, ?, ?);");
					stmt.setString(1, player.getUniqueId().toString());
					stmt.setString(2, test.getName());
					stmt.setBoolean(3, result);
					stmt.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(this);
	}
	
	/**
	 * Logs player's answer to the database.
	 * 
	 * @param player
	 *            player who answered
	 * @param question
	 *            question which has been answered
	 * @param answer
	 *            answer which has been chosen
	 */
	public void logAnswer(final Player player, final Test test,
			final Question question, final Answer answer) {
		if (!databaseUsed) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					PreparedStatement stmt = database.getConnection()
							.prepareStatement("INSERT INTO answers (player, " +
							"test, question, answer, correct) VALUES (?, ?, ?, ?, ?);");
					stmt.setString(1, player.getUniqueId().toString());
					stmt.setString(2, test.getName());
					stmt.setString(3, question.getQuestion());
					stmt.setString(4, answer.getText());
					stmt.setBoolean(5, answer.isCorrect());
					stmt.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(this);
	}
	
}
