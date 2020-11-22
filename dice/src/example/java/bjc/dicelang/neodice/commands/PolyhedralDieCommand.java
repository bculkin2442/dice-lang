package bjc.dicelang.neodice.commands;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

public class PolyhedralDieCommand implements Command {
	@Override
	public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
		if (!words.hasNext()) {
			throw new DieBoxException("Number of sides to polyhedral-die must be provided");
		} else {
			StatementValue sideValue = state.runStatement(words);
			
			if (sideValue.type == INTEGER) {
				int numSides = ((IntegerStatementValue)sideValue).value;
				
				if (numSides < 0) throw new DieBoxException("Number of sides to polyhedral-die was not valid (must be less than 0, was %d)", numSides);
				
				IDie<StatementValue> die = IDie
						.polyhedral(numSides)
						.transform(IntegerStatementValue::new);
				
				return new DieStatementValue(INTEGER, die);
			} else {
				throw new DieBoxException("Number of sides to polyhedral-die wasn't an integer (was %s, of type %s)",
						sideValue, sideValue.type);
			}
		}
	}
	
	@Override
	public String shortHelp() {
		return "create a single polyhedral die";
	}
	
	@Override
	public String longHelp() {
		return "Creates a single polyhedral die with a fixed number of sides";
	}
}