package main;

import java.util.ArrayList;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import tools.Adb;
import analysis.StaticInfo;
import concolic.Condition;
import concolic.ExecutionEngine;
import concolic.Operation;
import concolic.PathSummary;

public class Main {

	static StaticApp testApp;
	static Adb adb = new Adb();
	
	public static void main(String[] args) {
		
		String[] apps = {
/* 0 */			"/home/wenhaoc/AppStorage/Fast.apk",
				"/home/wenhaoc/AppStorage/APAC_engagement/backupHelper/backupHelper.apk",
				"/home/wenhaoc/AppStorage/APAC_engagement/Butane/Butane.apk",
/* 3 */			"/home/wenhaoc/AppStorage/APAC_engagement/CalcA/CalcA.apk",
				"/home/wenhaoc/AppStorage/APAC_engagement/KitteyKittey/KitteyKittey.apk",
/* 5 */			"/home/wenhaoc/AppStorage/net.mandaria.tippytipper.apk",
/* 6 */			"/home/wenhaoc/adt_workspace/TheApp/bin/TheApp.apk",
		};
		
		String apkPath = apps[6];
		
		testApp = StaticInfo.initAnalysis(apkPath, false);
		
		ExecutionEngine ee = new ExecutionEngine(testApp);
		
		testApp = ee.buildPathSummaries(false);
		
		testPathSummaries();
		
	}
	
	static void testPathSummaries() {
		System.out.println("-------------------- PS in StaticMethod");
		for (StaticClass c : testApp.getClasses())
			for (StaticMethod m : c.getMethods())
				if (m.getPathSummaries().size() > 0) {
					System.out.println("[Method]" + m.getSmaliSignature());
					System.out.println("[PS count]" + m.getPathSummaries().size());
				}
		System.out.println("\n------------------PS in StaticApp (count: " + testApp.getAllPathSummaries().size() + ")");
		ArrayList<String> mList = new ArrayList<String>();
		for (PathSummary ps : testApp.getAllPathSummaries())
			if (!mList.contains(ps.getMethodSignature())) {
				mList.add(ps.getMethodSignature());
				System.out.println("  " + ps.getMethodSignature());
			}
	}
	
	private void printOutPathSummary(PathSummary pS) {
		System.out.println("\n Execution Log: ");
		for (String s : pS.getExecutionLog())
			System.out.println("  " + s);
		System.out.println("\n Symbolic States: ");
		for (Operation o : pS.getSymbolicStates())
			System.out.println("  " + o.toString());
		System.out.println("\n PathCondition: ");
		for (Condition cond : pS.getPathCondition())
			System.out.println("  " + cond.toString());
		System.out.println("\n PathChoices: ");
		for (String pC : pS.getPathChoices())
			System.out.println("  " + pC);
		System.out.println("========================");
	}
	

}
