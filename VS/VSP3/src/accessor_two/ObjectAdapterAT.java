package accessor_two;

import java.util.ArrayList;
import java.util.Arrays;

import mware_lib.CommunicationModule;
import mware_lib.MessageADT;
import mware_lib.ObjectAdapter;

/**
 * 
 * @author Fabian
 * 
 *         Implementierung des Objekt-Adapters
 */
public class ObjectAdapterAT implements ObjectAdapter {

	public ObjectAdapterAT() {
		CommunicationModule.debugPrint(this.getClass(), "inizialized");
	}

	@Override
	public void initSkeleton(MessageADT m) {
		int objectNumber = m.getObjectRef().getObjectNumber();
		switch (objectNumber) {
		case SkeletonOneAT.ID:
			CommunicationModule.debugPrint(this.getClass(),
					"new skeleton created");
			SkeletonOneAT s = new SkeletonOneAT(m);
			s.start();
			break;
		default:
			CommunicationModule.debugPrint(this.getClass(),
					"can't create new skeleton");
			break;
		}
	}

	@Override
	public ArrayList<Integer> getSkeletonIDs() {
		return new ArrayList<Integer>(Arrays.asList(SkeletonOneAT.ID));
	}

}