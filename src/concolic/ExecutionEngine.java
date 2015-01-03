package concolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import tools.Adb;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;

public class ExecutionEngine {

	private StaticApp testApp = null;
	private Adb adb = new Adb();
	public boolean blackListOn = true;
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public StaticApp buildPathSummaries(boolean forceAllSteps) {
		
		adb.uninstallApp(testApp.getPackageName());
		adb.installApp(testApp.getSootAppPath());
		adb.startApp(testApp.getPackageName(), testApp.getMainActivity().getJavaName());
		UIModelGenerator builder = new UIModelGenerator(testApp);
		builder.buildOrRead(forceAllSteps);
		UIModelGraph model = builder.getUIModel();
		Execution ex = new Execution(testApp, builder.getExecutor());
		ex.blackListOn = this.blackListOn;
		
		for(Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
			
			String methodSig = entry.getKey();
			if (!methodSig.startsWith("Lthe/app/Irwin$3;->onClick"))
				continue;
			System.out.println("\n[Method]" + methodSig);
			System.out.println("[SeqFromMap]" + entry.getValue());
			
			List<Event> eventSeq = generateFullSequence(entry.getValue(), model);
			System.out.println("[EventSequence]" + eventSeq);
			
			System.out.println("===========concolic starts");
			ex.init();
			ex.setTargetMethod(methodSig);
			ex.setSequence(eventSeq);
			ArrayList<PathSummary> psList = ex.doConcolic();
			
			testApp.findMethod(methodSig).setPathSummaries(psList);
		}
		
		return testApp;
		
	}
	
	public StaticApp doFullSymbolic() {
		Execution ex = new Execution(testApp);
		ex.blackListOn = this.blackListOn;
		for (StaticClass c : testApp.getClasses()) {
			for (StaticMethod m : c.getMethods()) {
				ex.init();
				ex.setTargetMethod(m.getSmaliSignature());
				ArrayList<PathSummary> psList = ex.doFullSymbolic();
				testApp.findMethod(m.getSmaliSignature()).setPathSummaries(psList);
			}
		}
		return testApp;
	}
	
	
	
	
	private List<Event> generateFullSequence(List<Event> seqFromMap, UIModelGraph model) {
		List<Event> result = new ArrayList<Event>();
		List<Event> trimmedSeqFromMap = new ArrayList<Event>();
		for (Event e : seqFromMap) {
			if (e.getEventType() == Event.iLAUNCH)
				trimmedSeqFromMap = new ArrayList<Event>();
			trimmedSeqFromMap.add(e);
		}
		if (trimmedSeqFromMap.get(0).getEventType() == Event.iLAUNCH) {
			result.addAll(trimmedSeqFromMap);
		}
		else {
			UIState firstMainUI = model.getFirstMainUIState();
			List<Event> firstHalf = model.getEventSequence(firstMainUI, trimmedSeqFromMap.get(0).getSource());
			result.addAll(firstHalf);
			result.addAll(trimmedSeqFromMap);
		}
		return result;
	}
	
}
