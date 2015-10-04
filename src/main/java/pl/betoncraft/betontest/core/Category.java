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
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.exception.BoardException;
import pl.betoncraft.betontest.exception.CategoryException;
import pl.betoncraft.betontest.exception.QuestionException;

/**
 * Represents a category of question in the test. 
 *
 * @author Jakub Sapalski
 */
public class Category {
	
	private BetonTest plugin;
	private String name;
	private Test test;
	private int choices;
	private ArrayList<Board> boards = new ArrayList<>();
	private ArrayList<Question> questions = new ArrayList<>();
	private ArrayList<Question> available;
	private Location playerLoc;
	private ArrayList<UUID> players = new ArrayList<>();

	public Category(BetonTest plugin, Test test, String name, ConfigurationSection config)
			throws CategoryException {
		this.plugin = plugin;
		this.test = test;
		this.name = name;
		if (config == null) 
			throw new CategoryException("category not defined");
		choices = config.getInt("choices", -1);
		if (choices < 0) throw new CategoryException("amount of choices not defined");
		if (config.getConfigurationSection("boards") == null)
			throw new CategoryException("boards not defined");
		for (String boardName : config.getConfigurationSection("boards").getKeys(false)) {
			try {
				boards.add(new Board(plugin, this, config.getConfigurationSection("boards." + boardName)));
			} catch (BoardException e) {
				throw new CategoryException("error on '" + boardName + "' board: " 
						+ e.getMessage());
			}
		}
		if (boards.isEmpty())
			throw new CategoryException("boards not defined");
		if (config.getConfigurationSection("questions") == null)
			throw new CategoryException("questions not defined");
		for (String questionName : config.getConfigurationSection("questions").getKeys(false)) {
			try {
				questions.add(new Question(plugin, this, config.getConfigurationSection("questions." + questionName)));
			} catch (QuestionException e) {
				throw new CategoryException("error in '" + questionName + "' question: "
						+ e.getMessage());
			}
		}
		if (questions.isEmpty())
			throw new CategoryException("questions not defined");
		if (questions.size() < boards.size())
			throw new CategoryException("there are less questions than boards");
		String[] parts = config.getString("player_loc", "").split(";");
		if (parts.length < 6) 
			throw new CategoryException("not enough argumenst in player_loc");
		World world = Bukkit.getWorld(parts[3]);
		if (world == null) 
			throw new CategoryException("in player_loc, world does not exist");
		double x, y, z;
		float yaw, pitch;
		try {
			x = Double.parseDouble(parts[0]);
			y = Double.parseDouble(parts[1]);
			z = Double.parseDouble(parts[2]);
			yaw = Float.parseFloat(parts[4]);
			pitch = Float.parseFloat(parts[5]);
		} catch (NumberFormatException e) {
			throw new CategoryException("incorrect coordinates in player_loc");
		}
		playerLoc = new Location(world, x, y, z, yaw, pitch);
	}

	/**
	 * @return the boards
	 */
	public ArrayList<Board> getBoards() {
		return boards;
	}

	/**
	 * @return the available questions
	 */
	public ArrayList<Question> getAvailable() {
		return available;
	}

	/**
	 * @return how many choices this category will have in each question
	 */
	public int getChoices() {
		return choices;
	}
	
	/**
	 * @return true if player is doing this category
	 */
	public boolean hasPlayer(Player player) {
		return players.contains(player.getUniqueId());
	}
	
	/**
	 * @return the test which contains this category
	 */
	public Test getTest() {
		return test;
	}
	
	/**
	 * Shuffles all questions and displays them randomly on category boards.
	 */
	public void shuffle() {
		available = new ArrayList<>(questions);
		Random rand = new Random();
		for (Board board : boards) {
			int index = rand.nextInt(available.size());
			board.display(available.remove(index));
		}
	}

	/**
	 * Displays other question on the board.
	 */
	public void redraw(Board board) {
		Question last = board.getQuestion();
		if (available.isEmpty()) {
			board.display(last);
		} else {
			int index = new Random().nextInt(available.size());
			board.display(available.remove(index));
			available.add(last);
		}
	}

	/**
	 * Adds the player to this category.
	 */
	public void addPlayer(Player player) {
		plugin.getData().set(player.getUniqueId() + ".current.category", name);
		players.add(player.getUniqueId());
		player.teleport(playerLoc);
	}
	
	/**
	 * Loads the player into this category.
	 */
	public void loadPlayer(Player player) {
		players.add(player.getUniqueId());
	}

	/**
	 * Removes this player from the category.
	 */
	public void removePlayer(Player player) {
		players.remove(player.getUniqueId());
	}

	/**
	 * @return the name of the category
	 */
	public String getName() {
		return name;
	}
}
