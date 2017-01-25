package evaluation;

import java.io.PrintWriter;

//import java.io.PrintWriter;

import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import alignmentTest.AlignmentTest;
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

public class EvaluationTest 
{
	public EvaluationTest(String path, String log, String model) throws Exception
	{
		PrintWriter pw = new PrintWriter(path + "/Evaluation results/evaluationResults_" + model.substring(0,model.lastIndexOf(".")) + log.substring(0, log.lastIndexOf(".")) + ".csv");
		PrintWriter pw2 = new PrintWriter(path + "/Evaluation results/comparingAlignmentResults_" + model.substring(0,model.lastIndexOf(".")-1) + log.substring(0, log.lastIndexOf(".")-1) + ".txt");
		XLog xLog = new ImportEventLog().importEventLog(path + "/" + log);
		PetrinetGraph net = AlignmentTest.constructNet(path + "/" + model);
		Object[] pnet_and_marking = new ImportProcessModel().importPetriNetAndMarking(path + "/" + model);
		int it = 1;
		int numberOfIterations = 1;
		LongArrayList preparationAlignments = new LongArrayList();
		LongArrayList performanceAlignments = new LongArrayList();
		LongArrayList preparationDAFSA = new LongArrayList();
		LongArrayList performanceDAFSA = new LongArrayList();
		AlignmentTest allOptimal = null;
		ConstructPSP psp = null;
		
		//psp = new ConstructPSP(xLog, (Petrinet) pnet_and_marking[0], (Marking) pnet_and_marking[1]);
		
		int fstates =0;
		for(Place place : net.getPlaces())
		{
			if(net.getOutEdges(place).size()==0)
				fstates++;
		}
		if(fstates>1)
			pw.println("Petrinet " + path + "/" + model + " is not sound");
		else
		{
			pw.println("Iteration,All optimal Alignments preparation time,All optimal Alignments performance time,DAFSA preparation time, DAFSA performance time");
			while(it <= numberOfIterations)
			{
				System.out.println(it);
//				allOptimal = new AlignmentTest(xLog, net);
//				System.out.println("Alignment done");
				psp = new ConstructPSP(xLog, (Petrinet) pnet_and_marking[0], (Marking) pnet_and_marking[1]);
//				if(psp.psp().modelAutomaton().finalStates().size() > 1)
//				{
//					pw.println("Petrinet " + path + "/" + model + " is not sound");
//					break;
//				}
//				preparationAlignments.add(allOptimal.timePreparations);
//				performanceAlignments.add(allOptimal.timePerformance);
				preparationDAFSA.add(psp.preperationLog+psp.preperationModel);
				performanceDAFSA.add(psp.timePerformance);
				//pw.println(it + "," + preparationAlignments.getLast() + "," + performanceAlignments.getLast() + "," + preparationDAFSA.getLast() + "," + performanceDAFSA.getLast());
				it++;
			}
			pw.close();
			
//			pw2.println(allOptimal.replayResult.getInfo());
//			pw2.println(psp.replayResult().getInfo());
//			
//			pw2.println();
//			pw2.println("All optimal Alignments results:");
//			for (AllSyncReplayResult res : allOptimal.replayResult)
//			{
//				pw2.println(res.getInfo());
//				for(int i =0; i<res.getNodeInstanceLst().size(); i++)
//				{
//					pw2.println(res.getNodeInstanceLst().get(i));
//					pw2.println(res.getStepTypesLst().get(i));
//				}
//			}
//			
			pw2.println();
			pw2.println("PSP results:");
			for (AllSyncReplayResult res : psp.replayResult())
			{
				pw2.println(res.getInfo());
				for(int i =0; i<res.getNodeInstanceLst().size(); i++)
				{
					pw2.println(res.getNodeInstanceLst().get(i));
					pw2.println(res.getStepTypesLst().get(i));
				}
			}
			pw2.close();
//			System.out.println(preparationAlignments.average());
//			System.out.println(performanceAlignments.average());
			System.out.println(preparationDAFSA.average());
			System.out.println(performanceDAFSA.average());
			//System.out.println(allOptimal.mapping);
			System.out.println(psp.psp().logAutomaton().eventLabels());
			System.out.println(psp.psp().modelAutomaton().eventLabels());
		}	
	}
}
