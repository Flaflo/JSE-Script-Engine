package de.flaflo.jse.interpreter.actions;

import de.flaflo.jse.interpreter.methods.Method;

/**
 * Represents a queued Action to run a method
 * @author Flaflo
 *
 */
public class RunAction implements IAction {

	private final Method method;
	
	/**
	 * Constructs the Action
	 * @param method the method to be run
	 */
	public RunAction(final Method method) {
		this.method = method;
	}
	
	@Override
	public void run() {
		method.run();
	}

	/**
	 * Returns the holding method
	 * @return the holding method
	 */
	public Method getMethod() {
		return method;
	}
}
