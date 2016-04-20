package de.flaflo.jse.interpreter.methods;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import de.flaflo.jse.interpreter.Interpreter;
import de.flaflo.jse.interpreter.actions.IAction;
import de.flaflo.jse.interpreter.objects.ScriptVariable;

/**
 * Represents a Method in the Scriptengine
 * @author Flaflo
 *
 */
public class Method implements IAction {

	private final String[] body;
	private final ScriptVariable<?>[] parameter;
	
	private final Interpreter interpreter;

	/**
	 * Constructs a Method
	 * @param parameter the Parameters given to the method
	 * @param body the Script code running inside the method
	 */
	public Method(final ScriptVariable<?>[] parameter, final String[] body) {
		this.parameter = parameter;
		this.body = body;

		this.interpreter = new Interpreter(Arrays.stream(body).collect(Collectors.joining("\n")), parameter);
		try {
			this.interpreter.read();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.interpreter.run();
	}
	
	/**
	 * An Array of ScriptVariables that are given as Parameter
	 * @return
	 */
	public ScriptVariable<?>[] getParameter() {
		return parameter;
	}

	/**
	 * Returns the Script code executed inside the Method
	 * @return the Method Body
	 */
	public String[] getBody() {
		return this.body;
	}

	/**
	 * Returns the Script parser for the Method body
	 * @return the Interpreter
	 */
	public Interpreter getInterpreter() {
		return this.interpreter;
	}
}
