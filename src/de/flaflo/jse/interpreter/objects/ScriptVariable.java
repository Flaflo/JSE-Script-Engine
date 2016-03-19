package de.flaflo.jse.interpreter.objects;

/**
 * Represents an object variable in the Script
 * @author Flaflo
 *
 */
public final class ScriptVariable<T> {

	private final String name;
	private T value;
	
	/**
	 * Constructs the Variable
	 * @param name the Name of the Variable
	 * @param value the final Value of the Variable
	 */
	public ScriptVariable(final String name, final T value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the Value that has been stored
	 * @return the stored Value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the Value to be stored
	 * @param value the Value to be stored
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	/**
	 * Returns the final Name of the Variable
	 * @return the Name
	 */
	public String getName() {
		return name;
	}
}
