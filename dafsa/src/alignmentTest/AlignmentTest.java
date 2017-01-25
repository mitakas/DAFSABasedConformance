package alignmentTest;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsGraphAlg;
//import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsGraphILPAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsTreeAlg;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import nl.tue.astar.AStarException;

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

public class AlignmentTest 
{
	public TransEvClassMapping mapping = null;
	public static int iteration = 0;
	public PNMatchInstancesRepResult replayResult;
	public double cost;
	public long timePerformance;
	public long timePreparations;
	static {
		try {
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AlignmentTest(XLog xLog, PetrinetGraph net)
	{
		long start = System.currentTimeMillis();
		Marking initialMarking = null;
		Marking[] finalMarkings = null; // only one marking is used so far
		Map<Transition, Integer> costMOS = null; // movements on system
		Map<XEventClass, Integer> costMOT = null; // movements on trace
		
		
		initialMarking = getInitialMarking(net);
		finalMarkings = getFinalMarkings(net);
		//log = XParserRegistry.instance().currentDefault().parse(new File("d:/temp/BPI2013all90.xes.gz")).get(0);
		//			log = XParserRegistry.instance().currentDefault().parse(new File("d:/temp/BPI 730858110.xes.gz")).get(0);
		//			log = XFactoryRegistry.instance().currentDefault().openLog();
		costMOS = constructMOSCostFunction(net);
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		costMOT = constructMOTCostFunction(net, xLog, eventClassifier, dummyEvClass);
		mapping = constructMapping(net, xLog, dummyEvClass, eventClassifier);
//		System.out.println(initialMarking);
//		System.out.println(finalMarkings[0]);
//		System.out.println(mapping);
		long end = System.currentTimeMillis();
		this.timePreparations = end - start;
		int iteration = 0;
		while (iteration < 1) {
			//System.out.println("start: " + iteration);
			long itStart = System.currentTimeMillis();
			cost = computeCost(costMOS, costMOT, initialMarking, finalMarkings, null, net, xLog,
					mapping, true);
			long itEnd = System.currentTimeMillis();
			//System.out.println("   With ILP cost: " + cost1 + "  t: " + (mid - start));
			this.timePerformance = itEnd - itStart;
//
//			long mid2 = System.currentTimeMillis();
//			int cost2 = computeCost(costMOS, costMOT, initialMarking, finalMarkings, null, net, log,
//					mapping, false);
//			long end = System.currentTimeMillis();
//
//			System.out.println("   No ILP   cost: " + cost2 + "  t: " + (end - mid2));
//			if (cost1 != cost2)
//				System.err.println("ERROR");
//			else
//				cost = cost1;
			//System.gc();
			//System.out.flush();
			iteration++;
		}
	}
	
	public AlignmentTest(XLog log, String pnmlFile) throws Exception {
		//		DummyUIPluginContext context = new DummyUIPluginContext(new DummyGlobalContext(), "label");

		PetrinetGraph net = null;
		Marking initialMarking = null;
		Marking[] finalMarkings = null; // only one marking is used so far
		Map<Transition, Integer> costMOS = null; // movements on system
		Map<XEventClass, Integer> costMOT = null; // movements on trace
		TransEvClassMapping mapping = null;

		net = constructNet(pnmlFile);
		initialMarking = getInitialMarking(net);
		finalMarkings = getFinalMarkings(net);
		//log = XParserRegistry.instance().currentDefault().parse(new File("d:/temp/BPI2013all90.xes.gz")).get(0);
		//			log = XParserRegistry.instance().currentDefault().parse(new File("d:/temp/BPI 730858110.xes.gz")).get(0);
		//			log = XFactoryRegistry.instance().currentDefault().openLog();
		costMOS = constructMOSCostFunction(net);
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		costMOT = constructMOTCostFunction(net, log, eventClassifier, dummyEvClass);
		mapping = constructMapping(net, log, dummyEvClass, eventClassifier);
//		System.out.println(initialMarking);
//		System.out.println(finalMarkings[0]);
//		System.out.println(mapping);
		int iteration = 0;
		while (iteration < 1) {
			System.out.println("start: " + iteration);
			long start = System.currentTimeMillis();
			double cost1 = computeCost(costMOS, costMOT, initialMarking, finalMarkings, null, net, log,
					mapping, true);
			long mid = System.currentTimeMillis();
			System.out.println("   With ILP cost: " + cost1 + "  t: " + (mid - start));
			this.timePerformance = mid - start;
//
//			long mid2 = System.currentTimeMillis();
//			int cost2 = computeCost(costMOS, costMOT, initialMarking, finalMarkings, null, net, log,
//					mapping, false);
//			long end = System.currentTimeMillis();
//
//			System.out.println("   No ILP   cost: " + cost2 + "  t: " + (end - mid2));
//			if (cost1 != cost2)
//				System.err.println("ERROR");
//			else
				cost = cost1;
			//System.gc();
			//System.out.flush();
			iteration++;
		}
	}

	public double computeCost(Map<Transition, Integer> costMOS, Map<XEventClass, Integer> costMOT,
			Marking initialMarking, Marking[] finalMarkings, PluginContext context, PetrinetGraph net, XLog log,
			TransEvClassMapping mapping, boolean useILP) {
		AllOptAlignmentsGraphAlg replayEngine;
//		if (useILP) {
//			replayEngine = new AllOptAlignmentsTreeAlg();
//		} else {
			replayEngine = new AllOptAlignmentsGraphAlg();
//		}

		Object[] parameters = new Object[10]; //new CostBasedCompleteParam(, );
		parameters[0] = costMOS;
		parameters[2] = costMOT;
		parameters[1] = 10001000;//	parameters.setInitialMarking(initialMarking);
//		parameters.setFinalMarkings(finalMarkings[0]);
//		parameters.setGUIMode(false);
//		parameters.setCreateConn(false);
//		parameters.setNumThreads(8);

		double cost = 0;
		double numStates = 0;
		double numAlignments = 0;
		double traceFitness = 0;
		double queuedStates = 0;
		try {
			
			replayResult = replayEngine.replayLog(context, net, initialMarking, finalMarkings[0], log, mapping, parameters);
			
			for (AllSyncReplayResult res : replayResult) {
				cost += ( res.getInfo().get("Raw Fitness Cost").doubleValue()) * res.getTraceIndex().size();
				numStates += res.getInfo().get(PNMatchInstancesRepResult.NUMSTATES);
				numAlignments += res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
				res.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, 1 - res.getInfo().get("Raw Fitness Cost") / res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH));
				traceFitness += res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * res.getTraceIndex().size();
				queuedStates += res.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE);
			}
			
			replayResult.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + numStates);
			replayResult.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
			replayResult.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" + (traceFitness / log.size()));
			replayResult.addInfo("Raw Fitness Cost", "" + cost);
			replayResult.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates);
			
		} catch (AStarException e) {
			e.printStackTrace();
		}

		return cost;
	}

	public static PetrinetGraph constructNet(String netFile) {
		PNMLSerializer PNML = new PNMLSerializer();
		NetSystem sys = PNML.parse(netFile);

		//System.err.println(sys.getMarkedPlaces());

		//		int pi, ti;
		//		pi = ti = 1;
		//		for (org.jbpt.petri.Place p : sys.getPlaces())
		//			p.setName("p" + pi++);
		//		for (org.jbpt.petri.Transition t : sys.getTransitions())
		//				t.setName("t" + ti++);

		PetrinetGraph net = PetrinetFactory.newPetrinet(netFile);

		// places
		Map<org.jbpt.petri.Place, Place> p2p = new HashMap<org.jbpt.petri.Place, Place>();
		for (org.jbpt.petri.Place p : sys.getPlaces()) {
			Place pp = net.addPlace(p.toString());
			p2p.put(p, pp);
		}

		// transitions
		//int l = 0;
		Map<org.jbpt.petri.Transition, Transition> t2t = new HashMap<org.jbpt.petri.Transition, Transition>();
		for (org.jbpt.petri.Transition t : sys.getTransitions()) {
			Transition tt = net.addTransition(t.getLabel());
			tt.setInvisible(t.isSilent());
			t2t.put(t, tt);
		}

		// flow
		for (Flow f : sys.getFlow()) {
			if (f.getSource() instanceof org.jbpt.petri.Place) {
				net.addArc(p2p.get(f.getSource()), t2t.get(f.getTarget()));
			} else {
				net.addArc(t2t.get(f.getSource()), p2p.get(f.getTarget()));
			}
		}

		// add unique start node
		if (sys.getSourceNodes().isEmpty()) {
			Place i = net.addPlace("START_P");
			Transition t = net.addTransition("");
			t.setInvisible(true);
			net.addArc(i, t);

			for (org.jbpt.petri.Place p : sys.getMarkedPlaces()) {
				net.addArc(t, p2p.get(p));
			}
		}

		return net;
	}

	private static Marking[] getFinalMarkings(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		Marking[] finalMarkings = new Marking[1];
		finalMarkings[0] = finalMarking;

		return finalMarkings;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}

	private static Map<Transition, Integer> constructMOSCostFunction(PetrinetGraph net) {
		Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();

		for (Transition t : net.getTransitions())
			if (t.isInvisible() || t.getLabel().equals("") || t.getLabel().contains("Tau"))
				costMOS.put(t, 0);
			else
				costMOS.put(t, 1);

		return costMOS;
	}

	private static Map<XEventClass, Integer> constructMOTCostFunction(PetrinetGraph net, XLog log,
			XEventClassifier eventClassifier, XEventClass dummyEvClass) {
		Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			costMOT.put(evClass, 1);
		}

		//		costMOT.put(dummyEvClass, 1);

		return costMOT;
	}

	private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
			XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			//boolean mapped = false;

			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();
				//String id2 = id.substring(0, id.indexOf("+"));
				String model = t.getLabel();
				if (model.equals(id)) {
					mapping.put(t, evClass);
					//mapped = true;
					break;
				}
			}
//			if (!mapped && !t.isInvisible()) {
//				mapping.put(t, dummyEvClass);
//			}
		}
		return mapping;
	}
}
