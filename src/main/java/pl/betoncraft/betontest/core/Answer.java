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

/**
 * Represents a single answer, which can be correct or incorrect.
 *
 * @author Jakub Sapalski
 */
public class Answer {
	
	private String text;
	private String comment;
	private boolean correct;

	public Answer(String text, String comment, boolean correct) {
		this.text = text;
		this.comment = comment;
		this.correct = correct;
	}

	/**
	 * @return the text of the answer
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the comment to the answer
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return if the answer is correct
	 */
	public boolean isCorrect() {
		return correct;
	}

}
