package de.flaflo.jse;

import java.io.File;
import java.io.IOException;

import de.flaflo.jse.interpreter.Interpreter;

/**
 * Main class, where the program has to start
 * @author Flaflo
 *
 */
public class Main {

	/**
	 * The Programs Entry Point
	 * @param args the args given
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		final Interpreter scriptInterpreter = new Interpreter(new File("C:/Users/Flaflo/Desktop/testscript.jses"));
		final String result = scriptInterpreter.read();
		
		System.out.println(result);
		
		if (result == Interpreter.STRING_EMPTY)
			scriptInterpreter.run();
	}
	
}