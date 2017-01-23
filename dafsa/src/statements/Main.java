package statements;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import alignmentTest.AlignmentTest;
import evaluation.EvaluationTest;
import importAutomatonFromLogOrModel.ImportEventLog;
import psp.ConstructPSP;

@SuppressWarnings("unused")
public class Main {
	public static void main(String[] args) throws Exception
	{
		String path = "ibm_doubleLogs2";
//		String log = "RoadFines_real.xes";
//		String model = "Traffic fines_loop.pnml";
		//new EvaluationTest(path, log, model);
		
		File folder = new File(path);
		File[] pnmlFiles = folder.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name)
			{
				//return name.endsWith(".pnml");
				return name.endsWith("c.s00000044__s00001066.pnml");
			}
		});
		for(File pnml : pnmlFiles)
		{
			File[] logFiles = folder.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.contains(pnml.getName().substring(0,pnml.getName().length()-5)) && name.endsWith(".xes");
				}
			});
			System.out.println(pnml.getName());
			for(File xes : logFiles)
			{
				System.out.println(xes.getName());
				new EvaluationTest(path, xes.getName(), pnml.getName());
				//new ConstructPSP(path, xes.getName(), pnml.getName(), true);
			}
			System.out.println();
		}

//		path = "sap_tauless2";
//		System.out.println("SAP Collection:");
//		folder = new File(path);
//		pnmlFiles = folder.listFiles(new FilenameFilter(){
//			public boolean accept(File dir, String name)
//			{
//				return name.endsWith(".pnml");
//			}
//		});
//		for(File pnml : pnmlFiles)
//		{
//			File[] logFiles = folder.listFiles(new FilenameFilter()
//			{
//				public boolean accept(File dir, String name)
//				{
//					return name.contains(pnml.getName().substring(0,pnml.getName().length()-5)) && name.endsWith(".xes");
//				}
//			});
//			System.out.println(pnml.getName());
//			for(File xes : logFiles)
//			{
//				//new EvaluationTest(path, xes.getName(), pnml.getName());
//				System.out.println(xes.getName());
//			}
//			System.out.println();
//		}
		
//		long start = System.nanoTime();
//		IdentifyStatements identifiedStatements = new IdentifyStatements(psp.psp());
//		if(!identifiedStatements.statements().isEmpty());
//		{
//			System.out.println(identifiedStatements.statements().size());
//			identifiedStatements.statements().forEach(statement -> System.out.println(statement));
//		}
//		long end = System.nanoTime();
//		System.out.println("Identifying Statements: " + TimeUnit.MILLISECONDS.convert((end - start), TimeUnit.NANOSECONDS) + "ms");
		//System.out.println(psp.psp().logAutomaton().finalConfigurations().size());
	}
}
