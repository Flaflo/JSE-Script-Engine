package de.flaflo.jse.interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.flaflo.jse.interpreter.actions.IAction;
import de.flaflo.jse.interpreter.actions.PrintAction;
import de.flaflo.jse.interpreter.actions.RunAction;
import de.flaflo.jse.interpreter.methods.Method;
import de.flaflo.jse.interpreter.objects.ScriptVariable;

/**
 * Parses the Script form a file
 * @author Flaflo
 *
 */
public class Interpreter {

	/** Represents a constant variable for an empty string */
	public static final String STRING_EMPTY = "";
	
	/** Represents a constant variable for the prefix of commentaries in code, that will be ignored */
	private static final String COMMENTARY_PREFIX = "//";
	
	private final File file;
	private final String interpretableCode;
	
	private final List<ScriptVariable<?>> variables;
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
	public Interpreter(final String source, ScriptVariable<?>[] variables) {
		this.file = null;
		
		this.interpretableCode = source;
		
		this.variables = Arrays.asList(variables);
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

		long lineCount = 2;

		if ((this.interpretableCode != null) || scriptReader.readLine().equalsIgnoreCase("#jsescript")) {
			String line;
			
			//To read blocks of coode
			boolean readingBlock = false; //is reading a block

			String blockName = STRING_EMPTY; //Name of the block
			String readBlock = STRING_EMPTY; //Content of the block
			
			while ((line = scriptReader.readLine()) != null) {

				if (line.replaceAll("\t", "").startsWith(COMMENTARY_PREFIX)) // Kommentare ignorieren
					continue;
				
				if (!readingBlock && readBlock != STRING_EMPTY) { //If the block has ended add read block of code as method
					final Method meth = new Method(this.getVariables().toArray(new ScriptVariable<?>[this.getVariables().size()]), readBlock.replaceAll("\t", "").substring(0, readBlock.replaceAll("\t", "").length() - 1).split("\n")); //TODO Parameters
					final ScriptVariable<Method> varMeth = new ScriptVariable<Method>(blockName, meth);
					
					this.registerVariable(varMeth);
					
					readBlock = STRING_EMPTY;
					blockName = STRING_EMPTY;
				}
				
				if (readingBlock) {
					if (!line.equals("}")) { //Append line to codeblock if its not ending here
						readBlock += line + "\n";
						
						continue;
					} else
						readingBlock = false;
				}
				
				if (line.endsWith(")")) {
					final String methLine = line.replace("\t", "").replace("(", "").replace(")", "");
					this.getActions().add(new RunAction((Method) this.getMethods().stream().filter(meth -> meth.getName().equals(methLine)).findFirst().get().getValue()));
				}

				if (line.toLowerCase().startsWith("var")) { // Ist eine Variable?
					final String[] varInfo = line.replace("var ", "").split(" = ");

					final String varName = varInfo[0];
					final String varValue = varInfo[1];

					final String varValueLowerNoSpaces = varValue.toLowerCase().replace(" ", "");
					
					if (varValueLowerNoSpaces.endsWith("){")) { //Eine Methode
						readingBlock = true;
						
						final String rawBody = varValue.replace("function", "").replace("(", "").replace(")", "").replace("{", "").replaceFirst(" ", "").replaceFirst(" ", "");
						readBlock += rawBody + "\n";
						blockName = varName;						
					} else if (varValue.startsWith("\"") && varValue.endsWith("\"")) {
						final String varStringValue = varValue.replace("\"", "");

						this.registerVariable(new ScriptVariable<String>(varName, varStringValue));
					} else
						try {
							final int numbersOfPoints = varValue.length() - varValue.replace(".", "").length();

							if (numbersOfPoints < 1) { // Es ist ein integer
								final Integer varIntValue = Integer.parseInt(varValue);

								this.registerVariable(new ScriptVariable<Integer>(varName, varIntValue));
							} else if (numbersOfPoints == 1) { // Es ist ein double
								final Double varDoubleValue = Double.parseDouble(varValue);

								this.registerVariable(new ScriptVariable<Double>(varName, varDoubleValue));
							} else
								throw new NumberFormatException(); // Konnte Variable nicht definieren
						} catch (final NumberFormatException ex) {
							result = "Could not recognize variable \"" + varName + "\" on line " + lineCount;

							break;
						}
				}
				
				
				if (((this.file == null) && (this.interpretableCode != null)) && line.toLowerCase().startsWith("print")) { // Voreingebaute
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

				}

				lineCount++;
			}
			
			if (!readingBlock && readBlock != STRING_EMPTY) {
				final Method meth = new Method(this.getVariables().toArray(new ScriptVariable<?>[this.getVariables().size()]), readBlock.substring(0, readBlock.length() - 1).split("\n")); //TODO Parameters
				final ScriptVariable<Method> varMeth = new ScriptVariable<Method>(blockName, meth);
				
				this.registerVariable(varMeth);
				
				readBlock = STRING_EMPTY;
				blockName = STRING_EMPTY;
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
			if (((this.file != null) && (this.interpretableCode == null))) {
				long mainCounts = this.getMethods().stream().filter(meth -> meth.getName().equalsIgnoreCase("main")).count();
				if (mainCounts == 1) {
					final Object methObj = this.getMethods().stream().filter(meth -> meth.getName().equalsIgnoreCase("main")).findFirst().get().getValue();
					
					if (methObj instanceof Method) {
						final Method main = (Method) this.getMethods().stream().filter(meth -> meth.getName().equalsIgnoreCase("main")).findFirst().get().getValue();
						main.run();
					} else
						System.out.println("Could not find main");
				} else if (mainCounts == 0)
					System.out.println("Could not find main");
				else if (mainCounts > 1)
					System.out.println("Could not invoke main");
			} else
				this.getActions().forEach(act -> act.run());
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
	 * Returns all registered Variables for the script
	 * @return the list of variables
	 */
	public synchronized List<ScriptVariable<?>> getVariables() {
		return this.variables;
	}
	
	/**
	 * Returns a list of Methods that are declared in the Script file
	 * @return the list of Methods
	 */
	public synchronized List<ScriptVariable<?>> getMethods() {
		return this.getVariables().stream().filter(var -> var.getValue() instanceof Method).collect(Collectors.toList());
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