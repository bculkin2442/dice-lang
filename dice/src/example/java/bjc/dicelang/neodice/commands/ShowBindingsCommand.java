package bjc.dicelang.neodice.commands;

import static bjc.dicelang.neodice.statements.VoidStatementValue.*;

import java.util.*;

import bjc.dicelang.neodice.*;
import bjc.dicelang.neodice.statements.*;

public class ShowBindingsCommand implements Command {
  @Override
  public StatementValue execute(Iterator<String> words, DieBoxCLI state) {
    state.output.printf("Showing all %d variables currently bound:", state.bindings.size());
    state.bindings.forEach((name, bound) -> {
      state.output.printf("\t%t\t%s\n", name, bound);
    });
    
    return VOID_INST;
  }
  
  @Override
  public String shortHelp() {
    return "print out all the variable bindings";
  }
  
  @Override
  public String longHelp() {
    return "Prints out all of the variable bindings that exist at the moment";
  }
}