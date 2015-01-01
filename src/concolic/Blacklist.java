package concolic;

import java.util.ArrayList;
import java.util.Arrays;

public class Blacklist {

	/*
	 * Some of the classes or methods(especially android.support.v4/v7) 
	 * is way too long and way too irrelevant to the behavior or the app.
	 * Therefore they will be skipped when:
	 *  1. before starting to generate PathSummary
	 *  2. symbolic execution encounters InvokeStmt that invokes them,
	 *  3. concrete execution setting break points.
	 * This is only a temporary solution, to increase the efficiency of 
	 * concolic execution. Maybe in the future there will be a way to deal
	 * with these huge methods efficiently.
	 * */
	public static ArrayList<String> classes = (ArrayList<String>) Arrays.asList(
			"Landroid/support/v4/app/FragmentManagerImpl;"
			
	);
	
	
	public static ArrayList<String> methods = (ArrayList<String>) Arrays.asList(
			""
	);

	
	public static boolean classInBlackList(String className) {
		return classes.contains(className);
	}
	
	public static boolean methodInBlackList(String methodSig) {
		return methods.contains(methodSig);
	}

	
}
