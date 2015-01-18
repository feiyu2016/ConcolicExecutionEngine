package concolic;

import java.util.ArrayList;
import java.util.List;

import staticFamily.StaticApp;
import tools.Adb;
import tools.Jdb;
import zhen.version1.component.Event;
import zhen.version1.component.WindowInformation;
import zhen.version1.framework.Common;
import zhen.version1.framework.Executer;

public class Validation {

	private Jdb jdb = new Jdb();
	private Adb adb = new Adb();
	private StaticApp staticApp;
	
	private Executer executer;
	private List<Event> seq = new ArrayList<Event>();
	
	public boolean useAdb = true;
	public boolean verbose = true;
	
	private ArrayList<String> targetLines = new ArrayList<String>();
	private ArrayList<String> jdbResult = new ArrayList<String>();

	
	public Validation(StaticApp staticApp, Executer executer) {
		this.staticApp = staticApp;
		this.executer = executer;
	}
	
	public Validation(StaticApp staticApp) {
		this.staticApp = staticApp;
	}
	
	public boolean validateSequence(List<Event> seq, ArrayList<String> targetLines) {
		this.seq = seq;
		this.targetLines = targetLines;
		System.out.println("\nValidating Event Sequence:\n  " + seq + "\nFor Target Lines:\n  " + targetLines);
		
		try {
			
			preparation();
			
			applyFinalEvent();
			
			jdbResult = collectBPResult();
			
		}
		catch (Exception e) {e.printStackTrace();}
		
		boolean result =  compare(jdbResult, targetLines);
		
		if (this.verbose) {
			if (result)	System.out.println("\nValidation Result: Success. the event sequence triggered all target lines, in the same order as well.");
			else		System.out.println("\nValidation Result: Failed. jdb result and target line sequence mismatch.");
		}
		return result;
	}
	
	private boolean compare(ArrayList<String> A, ArrayList<String> B) {
		if (A.size() < 1 || B.size() < 1 || A.size() != B.size())
			return false;
		for (int i = 1, len = A.size(); i < len; i++)
			if (!A.get(i).equals(B.get(i)))
				return false;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////
	
	
	private ArrayList<String> collectBPResult() throws Exception{
		System.out.println("\nCollecting Breakpoint Hits...");
		ArrayList<String> result = new ArrayList<String>();
		String jdbLine = "";
		while (!jdbLine.equals("TIMEOUT")) {
			if (jdbLine.startsWith("Breakpoint hit: ")) {
				if (this.verbose)
					System.out.println("  [J]" + jdbLine);
				if (!jdbLine.startsWith("Breakpoint hit: Set breakpoint")){
					String lineInfo = parseJDBLine(jdbLine);
					result.add(lineInfo);
				}
				jdb.cont();
			}
			jdbLine = jdb.readLine();
			if (jdbLine == null)
				throw (new Exception("Jdb might have crashed."));
		}
		return result;
	}

	private String parseJDBLine(String jdbLine) {
		String bpInfo = jdbLine.substring(jdbLine.indexOf("Breakpoint hit: "));
		String methodInfo = bpInfo.split(", ")[1];
		String cN = methodInfo.substring(0, methodInfo.lastIndexOf("."));
		String lineInfo = bpInfo.split(", ")[2];
		String lineNo = lineInfo.substring(lineInfo.indexOf("=")+1, lineInfo.indexOf(" "));
		return cN + ":" + lineNo;
	}
	
	private void preparation() throws Exception{
		
		System.out.print("\nReinstalling and Restarting App...  ");
		adb.uninstallApp(staticApp.getPackageName());
		adb.installApp(staticApp.getSmaliAppPath());
		System.out.println("Done.");
		adb.unlockScreen();
		adb.pressHomeButton();
		adb.startApp(staticApp.getPackageName(), staticApp.getMainActivity().getJavaName());
		
		System.out.print("\nInitiating jdb...  ");
		jdb.init(staticApp.getPackageName());
		System.out.println("Done.");
		
		System.out.println("\nGoing to Target Layout...");
		
		for (int i = 0, len = seq.size()-1; i < len; i++) {
			Event e = seq.get(i);
			if (e.getEventType() != Event.iLAUNCH) {
				if (!this.useAdb) {
					System.out.println("[Executer]" + e);
					this.executer.applyEvent(e);
					WindowInformation.checkVisibleWindowAndCloseKeyBoard(executer);
				}
				else {
					String x = e.getValue(Common.event_att_click_x).toString();
					String y = e.getValue(Common.event_att_click_y).toString();
					adb.click(x + " " + y);
				}
			}
		}
				
		System.out.print("\nSetting Break Points for Target Lines... ");
		for (String s : this.targetLines) {
			String className = s.split(":")[0];
			int line = Integer.parseInt(s.split(":")[1]);
			jdb.setBreakPointAtLine(className, line);
		}
		System.out.println("Done.");
	}
	
	private void applyFinalEvent() {
		if (!this.useAdb)
			executer.applyEvent(seq.get(seq.size()-1));
		else {
			Event lastEvent = seq.get(seq.size()-1);
			String x = lastEvent.getValue(Common.event_att_click_x).toString();
			String y = lastEvent.getValue(Common.event_att_click_y).toString();
			adb.click(x + " " + y);
		}
			
	}

	public ArrayList<String> getJdbResult() {
		return jdbResult;
	}

	
}
