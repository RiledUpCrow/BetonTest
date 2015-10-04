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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.exception.BoardException;

/**
 * Represents a physical board, where Question is displayed. 
 *
 * @author Jakub Sapalski
 */
public class Board {
	
	public BetonTest plugin;
	public Category category;
	
	private ArrayList<Block> qSigns = new ArrayList<>();
	private ArrayList<Block> aSigns = new ArrayList<>();

	private Question question;
	private ArrayList<Answer> answers = new ArrayList<>();
	
	public Board(BetonTest plugin, Category category,
			ConfigurationSection config) throws BoardException {
		this.plugin = plugin;
		this.category = category;
		if (config == null)
			throw new BoardException("board not defined");
		String[] parts = config.getString("location", "").split(";");
		if (parts.length < 4) 
			throw new BoardException("not enough arguments in location");
		World world = Bukkit.getWorld(parts[3]);
		if (world == null) 
			throw new BoardException("world in location does not exist");
		double x, y, z;
		try {
			x = Double.parseDouble(parts[0]);
			y = Double.parseDouble(parts[1]);
			z = Double.parseDouble(parts[2]);
		} catch (NumberFormatException e) {
			throw new BoardException("incorrect coordinates in location");
		}
		Location loc = new Location(world, x, y, z);
		Direction dir = Direction.match(config.getString("direction"));
		if (dir == null)
			throw new BoardException("incorrect direction value");
		int length = category.getChoices();
		
		// set question signs
		for (int i = 0; i < length; i++) {
			qSigns.add(loc.getBlock());
			loc.getBlock().setType(Material.WALL_SIGN);
			Sign sign;
			try {
				sign = (Sign) loc.getBlock().getState();
			} catch (ClassCastException e) {
				throw new BoardException("cannot create sign");
			}
			org.bukkit.material.Sign data =
					(org.bukkit.material.Sign) sign.getData();
			Vector vec = null;
			switch (dir) {
			case NORTH:
				vec = new Vector(-1, 0, 0);
				data.setFacingDirection(BlockFace.NORTH);
				break;
			case SOUTH:
				vec = new Vector(1, 0, 0);
				data.setFacingDirection(BlockFace.SOUTH);
				break;
			case EAST:
				vec = new Vector(0, 0, -1);
				data.setFacingDirection(BlockFace.EAST);
				break;
			case WEST:
				vec = new Vector(0, 0, 1);
				data.setFacingDirection(BlockFace.WEST);
				break;
			}
			sign.setData(data);
			sign.update(true, false);
			loc.add(vec);
		}
		// set answer signs
		loc.add(new Vector(0, -1, 0));
		for (int i = 0; i < length; i++) {
			Vector vec = null;
			switch (dir) {
			case NORTH:
				vec = new Vector(1, 0, 0);
				break;
			case SOUTH:
				vec = new Vector(-1, 0, 0);
				break;
			case EAST:
				vec = new Vector(0, 0, 1);
				break;
			case WEST:
				vec = new Vector(0, 0, -1);
				break;
			}
			loc.add(vec);
			aSigns.add(loc.getBlock());
			loc.getBlock().setType(Material.WALL_SIGN);
			Sign sign;
			try {
				sign = (Sign) loc.getBlock().getState();
			} catch (ClassCastException e) {
				throw new BoardException("cannot create sign");
			}
			org.bukkit.material.Sign data =
					(org.bukkit.material.Sign) sign.getData();
			switch (dir) {
			case NORTH:
				data.setFacingDirection(BlockFace.NORTH);
				break;
			case SOUTH:
				data.setFacingDirection(BlockFace.SOUTH);
				break;
			case EAST:
				data.setFacingDirection(BlockFace.EAST);
				break;
			case WEST:
				data.setFacingDirection(BlockFace.WEST);
				break;
			}
			sign.setData(data);
			sign.update(true, false);
		}
		// return to starting location
		loc.add(new Vector(0, 1, 0));
	}
	
	/**
	 * Displays the question on the board with randomly choosen answers.
	 */
	public void display(Question question) {
		this.question = question;
		answers.clear();
		displayQuestion();
		ArrayList<Answer> availableCorrects = new ArrayList<>(
				question.getCorrects());
		ArrayList<Answer> availableIncorrects = new ArrayList<>(
				question.getIncorrects());
		Random rand = new Random();
		int correctPosition = rand.nextInt(category.getChoices());
		for (int i = 0; i < category.getChoices(); i++) {
			if (i == correctPosition) {
				answers.add(availableCorrects.remove(rand.nextInt(
						availableCorrects.size())));
			} else {
				answers.add(availableIncorrects.remove(rand.nextInt(
						availableIncorrects.size())));
			}
		}
		for (int i = 0; i < answers.size(); i++) {
			displayAnswer(i);
		}
	}
	
	private void displayQuestion() {
		ArrayList<String> lines = splitToLines(question.getQuestion());
		int counter = 0;
		for (Block block : qSigns) {
			Sign sign = (Sign) block.getState();
			for (int i = 0; i < 4; i++) {
				if (counter < lines.size()) {
					sign.setLine(i, "§1" + lines.get(counter));
					counter++;
				} else {
					sign.setLine(i, "");
				}
			}
			sign.update();
		}
	}
	
	private void displayAnswer(int number) {
		Sign sign = (Sign) aSigns.get(number).getState();
		Answer answer = answers.get(number);
		ArrayList<String> lines = splitToLines(answer.getText());
		sign.setLine(0, "§l" + new String(Character.toChars(65 + aSigns.size()
				- 1 - number)));
		for (int i = 1; i < 4; i++) {
			if (i-1 < lines.size()) {
				sign.setLine(i, lines.get(i-1));
			} else {
				sign.setLine(i, "");
			}
		}
		sign.update();
	}
	
	private ArrayList<String> splitToLines(String text) {
		String[] words = text.split(" ");
		ArrayList<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			if (line.length() + words[i].length() + 1 > 16) {
				lines.add(line.toString().trim());
				line = new StringBuilder();
			}
			line.append(words[i] + " ");
		}
		lines.add(line.toString().trim());
		return lines;
	}
	
	/**
	 * @return the answer signs
	 */
	public ArrayList<Block> getSigns() {
		return aSigns;
	}

	/**
	 * @return the displayed on the signs answers
	 */
	public ArrayList<Answer> getAnswers() {
		return answers;
	}

	/**
	 * @return the question currently on the board
	 */
	public Question getQuestion() {
		return question;
	}
	
	public enum Direction {
		NORTH, SOUTH, EAST, WEST;
		
		public static Direction match(String string) {
			if (string == null) return null;
			Direction dir = null;
			try {
				dir = valueOf(string.toUpperCase());
			} catch (IllegalArgumentException e) {}
			return dir;
		}
	}

}
