package accessor_one;

import mware_lib.CommunicationModule;

public class ClassOneAO extends ClassOneImplBase {

	public ClassOneAO() {
		// TODO Auto-generated constructor stub
		CommunicationModule.debugPrint(this.getClass(), "Object initialized");
	}

	@Override
	public String methodOne(String param1, int param2) throws SomeException112 {
		// TODO Auto-generated method stub
		CommunicationModule.debugPrint(this.getClass(), "methodOne called");
		if(param2 % 2 == 0){
			return param1 + " " + param2 + " % 2 == 0"; 
		}else{
			throw new SomeException112("ClassOneAO-methodOne-SomeException112:::(" + param2 + " % 2 != 0)");
		}
	}

}