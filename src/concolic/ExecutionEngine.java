package concolic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import main.Paths;
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
	public boolean useAdb = true;
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public List<PathSummary> buildPathSummaries(boolean forceAllStep, UIModelGenerator builder) {
		
		List<PathSummary> result = new ArrayList<PathSummary>();
		
		File objFile = new File(Paths.appDataDir + "/path.summary");
		
		if (forceAllStep || !objFile.exists()) {
			UIModelGraph model = builder.getUIModel();
			Execution ex = new Execution(testApp);
			ex.useAdb = this.useAdb;
			ex.blackListOn = this.blackListOn;
	
			for( Entry<String, List<Event>>  entry : builder.getEventMethodMap().entrySet() ){
				String methodSig = entry.getKey();
				List<Event> eventSeq = generateFullSequence(entry.getValue(), model);
				ex.init();
				ex.setTargetMethod(methodSig);
				ex.setSequence(eventSeq);
				ArrayList<PathSummary> psList = ex.doConcolic();
				result.addAll(psList);
			}
		}
		return result;
	}
	
	
	
	public List<PathSummary> doFullSymbolic() {
		Execution ex = new Execution(testApp);
		ex.blackListOn = this.blackListOn;
		List<PathSummary> result = new ArrayList<PathSummary>();
		for (StaticClass c : testApp.getClasses()) {
			for (StaticMethod m : c.getMethods()) {
				ex.init();
				ex.setTargetMethod(m.getSmaliSignature());
				ArrayList<PathSummary> psList = ex.doFullSymbolic(false);
				result.addAll(psList);
			}
		}
		return result;
	}

	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////

	private void savePSList() {
		
	}
	
	private ArrayList<PathSummary> loadPSList() {
		
		return null;
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
