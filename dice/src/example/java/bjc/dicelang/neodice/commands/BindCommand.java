package bjc.dicelang.neodice.commands;

import static bjc.dicelang.neodice.statements.StatementValue.Type.*;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

public class BindCommand implements Command {
  @Override
  public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
  	if (!words.hasNext()) {
			throw new DieBoxException("bind requires a name to bind the value to");
		}
		
		String name = words.next();
		
		StatementValue value = state.runStatement(words);
		
		if (state.doWarn && value.type == VOID) {
			state.output.printf("Warning: bound %s to the instance of void. Should you have provided a value?", name);
		}
		
		state.bindings.put(name, value);
		
		return value;
  }
  
  @Override
  public String shortHelp() {
  	return "bind a value to a name";
  }
  
  @Override
  public String longHelp() {
  	return "Binds a value to a name, and returns that name. Currently, all variables go into a single global scope, but this will probably change";
  }
}