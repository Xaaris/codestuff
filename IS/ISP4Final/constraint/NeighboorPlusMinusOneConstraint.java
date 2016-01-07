package constraint;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class NeighboorPlusMinusOneConstraint implements BinaryConstraint, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	public Set<Integer> operationBinary(Set<Integer> x, Set<Integer> y, String name) {
		Set<Integer> res = new HashSet<Integer>();
		int counter = 0;

		for(int item : x){
			for(int itemy : y){
				if((item + 1 == itemy) || (item - 1 == itemy)){
					break;
				}
				counter++;
			}
			if(counter == y.size()){
				res.add(item);
			}
			counter = 0;
		}
		
		return res;
	}

}