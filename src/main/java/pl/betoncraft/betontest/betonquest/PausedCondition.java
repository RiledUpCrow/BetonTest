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
package pl.betoncraft.betontest.betonquest;

import org.bukkit.entity.Player;

import pl.betoncraft.betonquest.Instruction;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.utils.PlayerConverter;
import pl.betoncraft.betontest.BetonTest;
import pl.betoncraft.betontest.core.Test;

/**
 * Checks if the player has a paused test.
 *
 * @author Jakub Sapalski
 */
public class PausedCondition extends Condition {

	private final Test test;
	private final BetonTest betonTest = BetonTest.getPlugin(BetonTest.class);

	public PausedCondition(Instruction instruction)
			throws InstructionParseException {
		super(instruction);
		String name = instruction.next();
		if (!name.equalsIgnoreCase("any")) {
			test = betonTest.getTests().get(name);
			if (test == null)
				throw new InstructionParseException("Test does not exist");
		} else {
			test = null;
		}
	}

	@Override
	public boolean check(String playerID) {
		Player player = PlayerConverter.getPlayer(playerID);
		if (test == null) {
			return betonTest.getPausedTest(player) != null;
		} else {
			Test paused = betonTest.getPausedTest(player);
			if (paused == null) return false;
			return paused.equals(test);
		}
	}

}
