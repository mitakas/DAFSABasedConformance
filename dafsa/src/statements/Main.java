package statements;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.HashBiMap;

import alignmentTest.AlignmentTest;
import evaluation.EvaluationTest;
import importAutomatonFromLogOrModel.ImportEventLog;
import importAutomatonFromLogOrModel.ImportProcessModel;
import psp.ConstructPSP;

/*
 * Copyright © 2009-2017 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

@SuppressWarnings("unused")
public class Main {
	public static void main(String[] args) throws Exception
	{
		
		String path = "Road Traffic";
		String log = "RoadFines_real.xes";
		String model = "Traffic fines_loop.pnml";
		//String model = "bp3.bpmn";
		//new ImportProcessModel().createFSMfromBPNMFileWithConversion(path + "/" + model, HashBiMap.create(), HashBiMap.create()).toDot(path + "/" + "test.dot");
		
		new EvaluationTest(path, log, model);
		//new ConstructPSP(path, log, model, true);
		
		//String path = "ibm_doubleLogs2";
		//System.out.println("IBM Collection:");
//		File folder = new File(path);
//		File[] pnmlFiles = folder.listFiles(new FilenameFilter(){
//			public boolean accept(File dir, String name)
//			{
//				//return name.endsWith(".pnml");
//				return name.endsWith("c.s00000044__s00001066.pnml");
//			}
//		});
//		for(File pnml : pnmlFiles)
//		{
//			File[] logFiles = folder.listFiles(new FilenameFilter()
//			{
//				public boolean accept(File dir, String name)
//				{
//					return name.contains(pnml.getName().substring(0,pnml.getName().length()-5)) && name.endsWith("20.xes");
//				}
//			});
//			System.out.println(pnml.getName());
//			for(File xes : logFiles)
//			{
//				System.out.println(xes.getName());
//				//new EvaluationTest(path, xes.getName(), pnml.getName());
//				//new ConstructPSP(path, xes.getName(), pnml.getName(), true);
//				//new AlignmentTest( new ImportEventLog() );
//			}
//			System.out.println();
//		}
		
		
		
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
