import java.util.ArrayList;
import java.util.List;

import ist.meic.pa.GenericFunctions.GFMethod;
import ist.meic.pa.GenericFunctions.GenericFunction;

public class Tester {

	public static void main(String[] args) {
		GenericFunction gf = new GenericFunction("tester");
		gf.addMethod(new GFMethod(){
			Object call(Integer o, Integer s){
				return o + s;
			}
		});
		
		System.out.println(gf.call(3, 4));
	}

}
