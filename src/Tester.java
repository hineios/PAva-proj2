import ist.meic.pa.GenericFunctions.GFMethod;
import ist.meic.pa.GenericFunctions.GenericFunction;

public class Tester {

	public static void main(String[] args) {
		GenericFunction gf = new GenericFunction("tester");
		gf.addMethod(new GFMethod(){
			@SuppressWarnings("unused")
			Object call(Integer o, Integer s){
				return o + s;
			}
		});
		
		System.out.println(gf.call(3.0, 2.0));
	}

}
