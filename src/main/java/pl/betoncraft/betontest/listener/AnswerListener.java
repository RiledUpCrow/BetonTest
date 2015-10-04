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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.core.Answer;
import pl.betoncraft.betontest.core.Board;
import pl.betoncraft.betontest.core.Category;
import pl.betoncraft.betontest.core.Test;

/**
 * Handles player's answers.
 *
 * @author Jakub Sapalski
 */
public class AnswerListener implements Listener {
	
	private BetonTest plugin;

	public AnswerListener(BetonTest plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onAnswer(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK &&
				event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (event.getClickedBlock().getType() != Material.WALL_SIGN) return;
		Test test = plugin.getActiveTest(event.getPlayer());
		if (test == null) return;
		Category category = test.getPlayerCategory(event.getPlayer());
		for (Board board : category.getBoards()) {
			int index = board.getSigns().indexOf(event.getClickedBlock());
			if (index < 0) continue;
			Answer answer = board.getAnswers().get(index);
			if (answer.getComment().length() > 0)
				event.getPlayer().sendMessage(answer.getComment()
						.replace('&', '§'));
			if (answer.isCorrect()) {
				test.correct(event.getPlayer());
			} else {
				test.incorrect(event.getPlayer());
			}
			plugin.logAnswer(event.getPlayer(), test, board.getQuestion(), answer);
			category.redraw(board);
		}
	}
	
}
