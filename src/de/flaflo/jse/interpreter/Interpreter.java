package de.flaflo.jse.interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.flaflo.jse.interpreter.actions.IAction;
import de.flaflo.jse.interpreter.actions.PrintAction;
import de.flaflo.jse.interpreter.methods.Method;
import de.flaflo.jse.interpreter.objects.ScriptVariable;

/**
 * Parses the Script form a file
 * @author Flaflo
 *
 */
public class Interpreter {

	public static final String STRING_EMPTY = "";
	
	private final File file;
	private final String interpretableCode;
	
	private final ArrayList<ScriptVariable<?>> variables;
	private final ArrayList<IAction> actions;
	
	private boolean compiled;
	
	/**
	 * Constructs the Interpreter
	 * @param file The Scriptfile
	 */
	public Interpreter(final File file) {
		this.file = file;
		
		this.interpretableCode = null;

		
		this.variables = new ArrayList<ScriptVariable<?>>();
		this.actions = new ArrayList<IAction>();
	}
	
	/**
	 * Constructs the Interpreter
	 * @param file The Script source
	 */
	public Interpreter(final String source) {
		this.file = null;
		
		this.interpretableCode = source;
		
		this.variables = new ArrayList<ScriptVariable<?>>();
		this.actions = new ArrayList<IAction>();
	}
	
	/**
	 * Reads and parses the Scriptfile
	 * @return Normally nothing</br>
	 * 		   but when an error occurs it returns its.
	 * @throws IOException 
	 */
	public String read() throws IOException {
		String result = STRING_EMPTY;

		BufferedReader scriptReader = null;

		if ((this.file != null) && (this.interpretableCode == null))
			scriptReader = new BufferedReader(new FileReader(this.file));
		else if ((this.file == null) && (this.interpretableCode != null))
			scriptReader = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(this.interpretableCode.getBytes())));

		final String scriptHeader = scriptReader.readLine();

		long lineCount = 2;

		if (scriptHeader.equalsIgnoreCase("#jsescript")) {

			String line;

			while ((line = scriptReader.readLine()) != null) {

				if (line.startsWith("//")) // Kommentar
					continue;

				if (line.toLowerCase().startsWith("var")) { // Ist eine
															// Variable?
					final String[] varInfo = line.replace("var ", "").split(" = ");

					final String varName = varInfo[0];
					final String varValue = varInfo[1];

					if (varValue.startsWith("\"") && varValue.endsWith("\"")) {
						final String varStringValue = varValue.replace("\"", "");

						this.registerVariable(new ScriptVariable<String>(varName, varStringValue));
					} else
						try {
							final int numbersOfPoints = varValue.length() - varValue.replace(".", "").length();

							if (numbersOfPoints < 1) { // Es ist ein integer
								final Integer varIntValue = Integer.parseInt(varValue);

								this.registerVariable(new ScriptVariable<Integer>(varName, varIntValue));
							} else if (numbersOfPoints == 1) { // Es ist ein
																// double
								final Double varDoubleValue = Double.parseDouble(varValue);

								this.registerVariable(new ScriptVariable<Double>(varName, varDoubleValue));
							} else
								throw new NumberFormatException(); // Konnte
							// Variable
							// nicht
							// definieren
						} catch (final NumberFormatException ex) {
							result = "Could not recognize variable \"" + varName + "\" on line " + lineCount;

							break;
						}
				} else if (line.toLowerCase().startsWith("print")) { // Voreingebaute
																		// methode
					final String printParameter = line.replace("print ", "");

					if (printParameter.startsWith("\"") && printParameter.endsWith("\""))
						this.registerAction(new PrintAction(printParameter.replace("\"", "")));
					else if (!(printParameter.startsWith("\"") && printParameter.endsWith("\""))
							&& this.getVariables().stream().anyMatch(var -> printParameter.equals(var.getName())))
						this.registerAction(new PrintAction(String.valueOf(this.getVariables().stream()
								.filter(var -> printParameter.equals(var.getName())).findFirst().get().getValue())));
					else {
						result = "Could not recognize action \"print\" on line " + lineCount;

						break;
					}

				} else {

				}

				lineCount++;
			}
		} else
			result = "Could not recognize Script (Missing Header)";

		scriptReader.close();
		
		this.compiled = true;
		
		return result;
	}

	/**
	 * Runs the interpreted Script
	 * @return Normally the running time of the script</br>
	 * 		   but when an error occurs it returns its</br>
	 * 		   error code.
	 */
	public int run() {
		long scriptStartTime = System.currentTimeMillis();
		{
			for (final IAction act : this.getActions())
				act.run();
		}
		return (int) (System.currentTimeMillis() - scriptStartTime);
	}
	
	/**
	 * Registers a variable object
	 * @param var the variable object to register
	 */
	public synchronized void registerVariable(final ScriptVariable<?> var) {
		this.getVariables().add(var);
	}
	
	/**
	 * Registers an Action
	 * @param var the Action object to register
	 */
	public synchronized void registerAction(final IAction act) {
		this.getActions().add(act);
	}
	
	/**
	 * Returns all Methods stored as action
	 * @return the List of Methods
	 */
	public synchronized Method[] getMethods() {
		return (Method[]) this.actions.stream().filter(act -> act instanceof Method).toArray();
	}
	
	/**
	 * Returns all registered Variables for the script
	 * @return the list of variables
	 */
	public synchronized ArrayList<ScriptVariable<?>> getVariables() {
		return this.variables;
	}
	
	/**
	 * Returns all registered Actions for the script
	 * @return the list of Actions
	 */
	public synchronized ArrayList<IAction> getActions() {
		return this.actions;
	}

	/**
	 * Returns the Scriptfile to be interpreted from
	 * @return The Scriptfile
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Returns wether the Script has been interpreted or not
	 * @return is the Script interpreted
	 */
	public boolean isCompiled() {
		return compiled;
	}
}