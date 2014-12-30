package concolic;

import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import tools.Adb;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;

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
		
		//UIModelGraph model = builder.getUIModel();
		//model.enableGUI();
		
		//System.out.println("getEventDeposit");
		//for(Event e: builder.getEventDeposit()){
		//	System.out.println(e);
		//}

		//System.out.println("getEventMethodMap");
		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			System.out.println(entry);
			String methodSig = entry.getKey();
			List<Event> eventSeq = entry.getValue();
			System.out.println("\n[Method]" + methodSig);
			System.out.println("[EventSequence]" + eventSeq);
		}
		

/*		cE.setTargetMethod("Lthe/app/Irwin$3;->onClick(Landroid/view/View;)V");
		ArrayList<PathSummary> methodPSList = cE.doFullSymbolic();
		this.testApp.findMethod(eventHandlerMethodSig).setPathSummaries(methodPSList);*/
		
		return this.testApp;
		
	}
	
}
