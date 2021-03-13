package bjc.dicelang.neodice;

import java.io.*;

import org.junit.*;

@SuppressWarnings("javadoc")
public class DieBoxCLITest {
	//private DieBoxCLI diebox;
	
	private OutputStream dieBoxInput;
	private InputStream  dieBoxOutput;
	
	@Before
	public void setUp() throws Exception {
		PipedInputStream pipeInput = new PipedInputStream();
		dieBoxInput = new PipedOutputStream(pipeInput);
		
		PipedOutputStream pipeOutput = new PipedOutputStream();
		dieBoxOutput = new PipedInputStream(pipeOutput);
		
		//diebox = new DieBoxCLI(pipeInput, pipeOutput);
	}

	@After
	public void tearDown() throws Exception {
		dieBoxInput.close();
		dieBoxOutput.close();
	}
	
	// @TODO write some tests
}
