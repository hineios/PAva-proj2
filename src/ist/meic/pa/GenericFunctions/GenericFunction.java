package ist.meic.pa.GenericFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import java.lang.reflect.*;

public class GenericFunction {
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

	private Class<?>[] KeyToClasses(String key) {
		String[] keys = key.split(",");
		Class<?>[] c = new Class<?>[keys.length];
		int i = 0;

		for (String className : keys) {
			try {
				c[i] = Class.forName(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
			i++;
		}
		return c;
	}

	private Method getCallMethod(GFMethod gf) {
		Class<?> gfClass = gf.getClass();
		return gfClass.getDeclaredMethods()[0];
	}

	private Class<?>[][] getAllAvailableMethods(TreeMap<String, GFMethod> allMethods, int length) {
		Set<String> availableKeys = allMethods.keySet();
		Class<?>[][] available = new Class<?>[availableKeys.size()][length];
		int i = 0;
		for (String key : availableKeys) {
			Class<?>[] c = KeyToClasses(key);
			if (c == null) {
				continue;
			} else {
				available[i] = c;
				i++;
			}
		}
		return available;
	}

	public void addMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		primary.put(ClassesToKey(c), gf);
	}

	public void addAfterMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		after.put(ClassesToKey(c), gf);
	}

	public void addBeforeMethod(GFMethod gf) {
		Class<?>[] c = getCallMethod(gf).getParameterTypes();
		before.put(ClassesToKey(c), gf);
	}

	private ArrayList<Class<?>[]> getApplicableMethods(Class<?>[] args, TreeMap<String, GFMethod> allMethods) {
		Class<?>[][] available = null;
		ArrayList<Class<?>[]> applicable = null;
		available = getAllAvailableMethods(allMethods, args.length);
		applicable = new ArrayList<Class<?>[]>();
		boolean app = true;
		for (Class<?>[] a : available) {
			app = true;
			for (int i = 0; i < args.length; i++) {
				if (!a[i].isAssignableFrom(args[i])) {
					app = false;
					break;
				}
			}
			if (app) {
				applicable.add(a);
			}
		}
		return applicable;
	}

	private ArrayList<Class<?>[]> orderMethodsSpecificFirst(ArrayList<Class<?>[]> methods) {
		methods.sort(new Comparator<Class<?>[]>() {
			@Override
			public int compare(Class<?>[] arg0, Class<?>[] arg1) {
				int state = 0;
				for (int i = 0; i < arg0.length; i++) {
					if (arg0[i].equals(arg1[i])) {
						continue;
					} else if (arg0[i].isAssignableFrom(arg1[i])) {
						return 1;
					} else {
						return -1;
					}
				}
				return state;
			}
		});
		return methods;
	}

	private ArrayList<Class<?>[]> orderMethodsSpecificLast(ArrayList<Class<?>[]> methods) {
		methods.sort(new Comparator<Class<?>[]>() {
			@Override
			public int compare(Class<?>[] arg0, Class<?>[] arg1) {
				int state = 0;
				for (int i = 0; i < arg0.length; i++) {
					if (arg0[i].equals(arg1[i])) {
						continue;
					} else if (arg0[i].isAssignableFrom(arg1[i])) {
						return -1;
					} else {
						return 1;
					}
				}
				return state;
			}
		});
		return methods;
	}

	private static String print(Object obj) {

		if (obj instanceof Object[]) {
			return Arrays.deepToString((Object[]) obj);
		} else {
			return obj.toString();
		}
	}

	private String printSignature(Class<?>[] classes) {
		String ret = "[class ";
		int i = 1;
		for (Class<?> c : classes) {
			ret = ret + c.getName();
			if (i++ == classes.length) {
				ret = ret + "]";
			}else{
				ret = ret + ", class ";
			}
		}
		return ret;
	}

	private Object computeActualMethod(ArrayList<Class<?>[]> bMethods, ArrayList<Class<?>[]> pMethods,
			ArrayList<Class<?>[]> aMethods, Object[] args, Class<?>[] k) {
		GFMethod gM;
		Method m;
		Object ret = null;

		// Before Methods
		if (!bMethods.isEmpty()) {
			for (Class<?>[] bM : bMethods) {
				gM = before.get(ClassesToKey(bM));
				m = getCallMethod(gM);
				m.setAccessible(true);
				try {
					m.invoke(gM, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}

		// Call Primary Method
		if (!pMethods.isEmpty()) {
			gM = primary.get(ClassesToKey(pMethods.get(0)));
			m = getCallMethod(gM);
			m.setAccessible(true);
			try {
				ret = m.invoke(gM, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			String error = "No methods for generic function " + this.name + "with args " + print(args);
			error = error + " of classes " + printSignature(k);
			throw new IllegalArgumentException(error);
		}

		// After Methods
		if (!aMethods.isEmpty()) {
			for (Class<?>[] aM : aMethods) {
				gM = after.get(ClassesToKey(aM));
				m = getCallMethod(gM);
				m.setAccessible(true);
				try {
					m.invoke(gM, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		return ret;
	}

	public Object call(Object... args) {
		Class<?>[] k = new Class<?>[args.length];
		int i = 0;
		for (Object a : args) {
			k[i] = a.getClass();
			i++;
		}

		ArrayList<Class<?>[]> primaryMethods = getApplicableMethods(k, primary);
		ArrayList<Class<?>[]> beforeMethods = getApplicableMethods(k, before);
		ArrayList<Class<?>[]> afterMethods = getApplicableMethods(k, after);

		primaryMethods = orderMethodsSpecificFirst(primaryMethods);
		beforeMethods = orderMethodsSpecificFirst(beforeMethods);
		afterMethods = orderMethodsSpecificLast(afterMethods);

		return computeActualMethod(beforeMethods, primaryMethods, afterMethods, args, k);
	}
}
