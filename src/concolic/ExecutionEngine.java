package concolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;

public class ExecutionEngine {

	private StaticApp testApp = null;
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public StaticApp buildPathSummaries() {
		
		Execution cE = new Execution(testApp);
		cE.printOutPS = false;
		///////////////
		UIModelGenerator builder = new UIModelGenerator(testApp);
		builder.buildOrRead(false);
		
		UIModelGraph model = builder.getUIModel();
		model.enableGUI();
		
		System.out.println("getEventDeposit");
		for(Event e: builder.getEventDeposit()){
			System.out.println(e);
		}

		System.out.println("getEventMethodMap");
		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			System.out.println(entry);
		}
		//////////////////
/*		String eventHandlerMethodSig = "Lthe/app/Irwin$3;->onClick(Landroid/view/View;)V";
		
		cE.setTargetMethod(eventHandlerMethodSig);
		ArrayList<PathSummary> methodPSList = cE.doFullSymbolic();
		this.testApp.findMethod(eventHandlerMethodSig).setPathSummaries(methodPSList);*/
		
		return this.testApp;
		
	}
	
}
