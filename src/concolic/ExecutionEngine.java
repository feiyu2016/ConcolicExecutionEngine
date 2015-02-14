package concolic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import staticFamily.StaticApp;
import staticFamily.StaticClass;
import staticFamily.StaticMethod;
import zhen.version1.UIModelGenerator;
import zhen.version1.component.Event;
import zhen.version1.component.UIModelGraph;
import zhen.version1.component.UIState;

public class ExecutionEngine {

	private StaticApp testApp = null;
	public boolean blackListOn = true;
	public boolean useAdb = true;
	public boolean debug = false;
	
	public ExecutionEngine(StaticApp testApp) {
		this.testApp = testApp;
	}
	
	public ArrayList<PathSummary> buildPathSummaries(boolean forceAllStep, UIModelGenerator builder) {
		
		ArrayList<PathSummary> result = new ArrayList<PathSummary>();
		
		File objFile = new File(testApp.outPath + "/path.summaries");

		if (forceAllStep || !objFile.exists()) {
			
			System.out.println("\nStarting Concolic Execution Engine...");
			
			UIModelGraph model = builder.getUIModel();
			
			//Execution ex = new Execution(testApp);
			Execution ex = new Execution(testApp, builder.getExecutor());
			ex.useAdb = this.useAdb;
			ex.blackListOn = this.blackListOn;
			ex.debug = this.debug;
			for( Entry<String, List<Event>>  entry : builder.getMethodEventMap().entrySet()) {
				
				String methodSig = entry.getKey();
/*				if (!methodSig.equals("Lnet/mandaria/tippytipperlibrary/activities/TippyTipper;->onCreate(Landroid/os/Bundle;)V"))
					continue;*/
				List<Event> eventSeq = generateFullSequence(entry.getValue(), model);
				
				ex.init();
				ex.setTargetMethod(methodSig);
				ex.setSequence(eventSeq);
				
				ArrayList<PathSummary> psList = ex.doConcolic();
				result.addAll(psList);
				
			}
			
			savePSList(result);
		}
		else {
			result = loadPSList(builder);
		}
		
		return result;
	}
	
	
	
	public ArrayList<PathSummary> doFullSymbolic() {
		Execution ex = new Execution(testApp);
		ex.blackListOn = this.blackListOn;
		ex.debug = this.debug;
		ArrayList<PathSummary> result = new ArrayList<PathSummary>();
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

	private void savePSList(ArrayList<PathSummary> psList) {
		File objFile = new File(testApp.outPath + "/path.summaries");
		if (objFile.exists())
			objFile.delete();
		System.out.print("\nSaving Path Summaries into file... ");
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(objFile));
			out.writeObject(psList);
			out.close();
			System.out.print("Done.\n");
		}	catch (Exception e) {e.printStackTrace();}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<PathSummary> loadPSList(UIModelGenerator builder) {
		ArrayList<PathSummary> result = new ArrayList<PathSummary>();
		File objFile = new File(testApp.outPath + "/path.summaries");
		System.out.print("\nLoading Path Summaries... ");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(objFile));
			result = (ArrayList<PathSummary>) in.readObject();
			in.close();
			System.out.print("Done.\n");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			if (e.getMessage().contains("local class incompatible")) {
				result = buildPathSummaries(true, builder);
			}
			else
				e.printStackTrace();
		}
		return result;
	}
	
	public List<Event> generateFullSequence(List<Event> seqFromMap, UIModelGraph model) {
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
