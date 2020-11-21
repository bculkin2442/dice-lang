package bjc.dicelang.neodice.commands;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

public class RollCommand implements Command {
	@Override
	public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
		if (!words.hasNext()) {
			throw new DieBoxException("Roll must be provided an argument to roll");
		} else {
			StatementValue toRoll = state.runStatement(words);
			
			if (toRoll.type == DIE) {
				DieStatementValue die = (DieStatementValue) toRoll;
				
				return new IntegerStatementValue(die.value.roll(state.rng));
			} else if (toRoll.type == DIEPOOL) {
				DiePoolStatementValue pool = (DiePoolStatementValue) toRoll;
				
				return new IntArrayStatementValue(pool.value.roll(state.rng));
			} else {
				throw new DieBoxException("Roll was provided something that wasn't rollable (only DIE and DIEPOOL objects are rollable) (was %s, of type %s)",
						toRoll, toRoll.type);
			}
		}
	}
	
	@Override
	public String shortHelp() {
		return "rolls a die-like object";
	}
	
	@Override
	public String longHelp() {
		return "Rolls a die-like object, and yields the result of rolling it."
				+ " What is returned can differ based on what is rolled";
	}
}