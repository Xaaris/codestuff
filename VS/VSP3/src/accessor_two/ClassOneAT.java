package accessor_two;

import mware_lib.CommunicationModule;

/**
 * 
 * @author Fabian
 * 
 *         Servant-Klasse: Implementiert die eigentliche Funktionalität der
 *         Methoden
 */

public class ClassOneAT extends ClassOneImplBase {

	public ClassOneAT() {
		CommunicationModule.debugPrint(this.getClass(), "Object initialized");
	}

	@Override
	public double methodOne(String param1, double param2)
			throws SomeException112 {
		if (param2 < 2) {
			throw new SomeException112("param2 is less than 2");
		}
		CommunicationModule.debugPrint(this.getClass(), "methodOne called");
		return 2.2;
	}

	@Override
	public double methodTwo(String param1, double param2)
			throws SomeException112, SomeException304 {
		if (param1.equals("the monkey without shoes")) {
			throw new SomeException304("wow nice monkey ;)");
		} else if (param2 > 2) {
			throw new SomeException112("param2 is greater than 2");
		}
		CommunicationModule.debugPrint(this.getClass(), "methodTwo called");
		return 3.3;
	}

}