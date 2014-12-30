package concolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import tools.Adb;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;

public class ExecutionEngine {

	private StaticApp testApp = null;
	private Adb adb = new Adb();
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public StaticApp buildPathSummaries(boolean forceAllSteps) {
		
		adb.uninstallApp(testApp.getPackageName());
		adb.installApp(testApp.getSootAppPath());
		
		Execution cE = new Execution(testApp);
		cE.printOutPS = false;
		
		UIModelGenerator builder = new UIModelGenerator(testApp);
		builder.buildOrRead(forceAllSteps);
		
		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			String methodSig = entry.getKey();
			List<Event> eventSeq = entry.getValue();
			System.out.println("\n[Method]" + methodSig);
			System.out.println("[EventSequence]" + eventSeq);
			Execution ex = new Execution(testApp);
			ex.setTargetMethod(methodSig);
			ex.setSequence(eventSeq);
			ArrayList<PathSummary> psList = ex.doConcolic();
			this.testApp.findMethod(methodSig).setPathSummaries(psList);
		}
		
		return this.testApp;
		
	}
	
}
