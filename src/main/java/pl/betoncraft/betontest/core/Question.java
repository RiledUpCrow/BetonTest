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

import org.bukkit.configuration.ConfigurationSection;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.exception.QuestionException;

/**
 * Represents a question which can be displayed on a Board. 
 *
 * @author Jakub Sapalski
 */
public class Question {
	
	private String question;
	private ArrayList<Answer> corrects = new ArrayList<>();
	private ArrayList<Answer> incorrects = new ArrayList<>();
	private String prefix;
	
	public Question(BetonTest plugin, Category category, ConfigurationSection config)
			throws QuestionException {
		if (config == null)
			throw new QuestionException("question not defined");
		question = config.getString("text");
		if (question == null)
			throw new QuestionException("question text not defined");
		prefix = config.getString("prefix", "").replace('&', '§');
		if (config.getConfigurationSection("correct") == null)
			throw new QuestionException("correct answers not defined");
		for (String key : config.getConfigurationSection("correct")
				.getKeys(false)) {
			String text = config.getString("correct." + key + ".text");
			String comment = config.getString("correct." + key + ".comment");
			if (text == null)
				throw new QuestionException("answer text not defined in " +
						"correct " + key);
			if (comment == null)
				throw new QuestionException("answer comment not defined in " +
						"correct " + key);
			corrects.add(new Answer(text, comment, true));
		}
		if (corrects.isEmpty())
			throw new QuestionException("correct answers not defined");
		if (config.getConfigurationSection("incorrect") == null)
			throw new QuestionException("incorrect answers not defined");
		for (String key : config.getConfigurationSection("incorrect")
				.getKeys(false)) {
			String text = config.getString("incorrect." + key + ".text");
			String comment = config.getString("incorrect." + key + ".comment");
			if (text == null)
				throw new QuestionException("answer text not defined in " +
						"incorrect " + key);
			if (comment == null)
				throw new QuestionException("answer comment not defined in " +
						"incorrect " + key);
			incorrects.add(new Answer(text, comment, false));
		}
		if (incorrects.isEmpty())
			throw new QuestionException("incorrect answers not defined");
		if (incorrects.size() < category.getChoices() - 1)
			throw new QuestionException("you need at least 3 incorrect answers");
	}

	/**
	 * @return the question text
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @return the list containing correct answers
	 */
	public ArrayList<Answer> getCorrects() {
		return new ArrayList<>(corrects);
	}

	/**
	 * @return the list containing incorrect answers
	 */
	public ArrayList<Answer> getIncorrects() {
		return new ArrayList<>(incorrects);
	}
	
	/**
	 * @return the prefix for coloring the question on board
	 */
	public String getPrefix() {
		return prefix;
	}

}
