package constsraintnet;

import util.NameValMapping;

public class DifferentConstraint extends Constraint {

	public DifferentConstraint(String name, Type val1, Type val2) {
		super(name, val1, val2);
		// TODO Auto-generated constructor stub
	}
	
	public DifferentConstraint(String name, String val1, String val2) {
		super(name, val1, val2);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isSatisfied(Type val1, Type val2) {
		// TODO Auto-generated method stub
		if(val1.getElem() != val2.getElem()){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isSatisfied(String v1, String v2) {
		// TODO Auto-generated method stub
		Type val1 = new Type(NameValMapping.map(v1));
		Type val2 = new Type(NameValMapping.map(v1));
		
		if(val1.getElem() != val2.getElem()){
			return true;
		}
		return false;
	}

}
