package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.TreeMap;
import java.lang.reflect.*;

public class GenericFunction {
	private boolean logger = true;
	private String name;
	private final TreeMap<String, GFMethod> primary;
	private final TreeMap<String, GFMethod> before;
	private final TreeMap<String, GFMethod> after;

	public GenericFunction(String name) {
		super();
		this.name = name;
		primary = new TreeMap<String, GFMethod>();
		before = new TreeMap<String, GFMethod>();
		after = new TreeMap<String, GFMethod>();
	}

	private String ClassesToKey(Class<?>[] c) {
		String s = "";
		for (Class<?> a : c) {
			s = s.concat(a.getName() + ",");
		}
		return s;
	}

	private Method getCallMethod(GFMethod gf) {
		Class<?> gfClass = gf.getClass();
		return gfClass.getDeclaredMethods()[0];
	}

	public void addMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		primary.put(ClassesToKey(c), gf);

		if (logger) {
			System.out
					.println("Inserted primary method on generic function " + name + " with args: " + ClassesToKey(c));
		}
	}

	public void addAfterMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		after.put(ClassesToKey(c), gf);

		if (logger) {
			System.out.println("Inserted after method on generic function " + name + " with args: " + ClassesToKey(c));
		}
	}

	public void addBeforeMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		before.put(ClassesToKey(c), gf);

		if (logger) {
			System.out.println("Inserted before method on generic function " + name + " with args: " + ClassesToKey(c));
		}
	}

	/*private GFMethod getApplicableMethod(Class<?>[] args) {
		GFMethod applicable = null;
		applicable = primary.get(ClassesToKey(args));
		
		if(applicable != null){
			return applicable;
		}
		
		Class<?>[] argsCopy = new Class<?>[args.length]; 
		System.arraycopy(args, 0, argsCopy, 0, args.length);
		
		for (Class<?> c : argsCopy) {
			applicable = primary.get
		}
		if (applicable == null) {
			throw new IllegalArgumentException(
					"No methods for generic function " + name + " with arguments " + ClassesToKey(args));
		}
		return applicable;
	}*/

	public Object call(Object... args) throws IllegalArgumentException {
		Class<?>[] k = new Class<?>[args.length];
		int i = 0;
		for (Object a : args) {
			k[i] = a.getClass();
			i++;
		}
		if (logger) {
			System.out.println("Call on generic function " + name + " with args: " + ClassesToKey(k));
		}

		GFMethod gf = primary.get(ClassesToKey(k));
		if (gf == null) {
			throw new IllegalArgumentException(
					"No methods for generic function " + name + " with arguments " + ClassesToKey(k));
		}
		Class<?> gfClass = gf.getClass();
		Method m = gfClass.getDeclaredMethods()[0];
		try {
			m.setAccessible(true);
			return m.invoke(gf, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return name;
	}
}
