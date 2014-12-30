package concolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import tools.Adb;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;

public class ExecutionEngine {

	private StaticApp testApp = null;
	private Adb adb = new Adb();
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public StaticApp buildPathSummaries(boolean forceAllSteps) {
		
		adb.uninstallApp(testApp.getPackageName());
		adb.installApp(testApp.getSootAppPath());

		UIModelGenerator builder = new UIModelGenerator(testApp);
		builder.buildOrRead(forceAllSteps);
		UIModelGraph model = builder.getUIModel();

		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			String methodSig = entry.getKey();
			List<Event> eventSeq = generateFullSequence(entry.getValue(), model);
			System.out.println("\n[Method]" + methodSig);
			System.out.println("[EventSequence]" + eventSeq);
			Execution ex = new Execution(testApp, builder.getExecutor());
			ex.setTargetMethod(methodSig);
			ex.setSequence(eventSeq);
			ArrayList<PathSummary> psList = ex.doConcolic();
			this.testApp.findMethod(methodSig).setPathSummaries(psList);
		}
		
		return this.testApp;
		
	}
	
	private List<Event> generateFullSequence(List<Event> seqFromMap, UIModelGraph model) {
		List<Event> result = new ArrayList<Event>();
		if (seqFromMap.get(0).getEventType() == Event.iLAUNCH) {
			result.addAll(seqFromMap);
		}
		else {
			UIState firstMainUI = model.getFirstMainUIState();
			List<Event> firstHalf = model.getEventSequence(firstMainUI, seqFromMap.get(0).getSource());
			result.addAll(firstHalf);
			result.addAll(seqFromMap);
		}
		return result;
	}
	
}
