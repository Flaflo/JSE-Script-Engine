package de.flaflo.jse.interpreter.actions;

/**
 * Represents the Action to print something into the console
 * @author Flaflo
 *
 */
public class PrintAction implements IAction {

	private final String thingToPrint;
	
	/**
	 * Constructs the PrintAction
	 * @param thingToPrint The String you want to print
	 */
	public PrintAction(String thingToPrint) {
		this.thingToPrint = thingToPrint;
	}
	
	@Override
	public void run() {
		System.out.println(thingToPrint);
	}

	/**
	 * Returns the String you want to print
	 * @return the String to print
	 */
	public String getThingToPrint() {
		return thingToPrint;
	}
}
