package psp;

import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.deckfour.xes.model.XLog;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import Automaton.Automaton;
import Automaton.Transition;
import importAutomatonFromLogOrModel.ImportEventLog;
import importAutomatonFromLogOrModel.ImportProcessModel;

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

public class ConstructPSP {
	
	private Automaton logAutomaton;
	private Automaton modelAutomaton;
	private PSP psp;
	private Map<IntArrayList, Set<Node>> prefixMemorizationTable;
	private Map<Long, Set<Configuration>> suffixMemorizationTable;
	private PNMatchInstancesRepResult replayResult;
	private Map<Integer, Set<Multiset<Integer>>> optimalModelFutures;
	public  int cost = 0;
	public long preperationLog;
	public long preperationModel;
	public long timePerformance;
	
	public ConstructPSP(XLog xLog, Petrinet pnet, Marking marking) throws IOException, ConnectionCannotBeObtained
	{
		long start = System.currentTimeMillis();
		logAutomaton = new ImportEventLog().createDAFSAfromLog(xLog);
		long logTime = System.currentTimeMillis();
		this.preperationLog = logTime - start;
		modelAutomaton = new ImportProcessModel().createFSMfromPetrinet(pnet, marking, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
		long modelTime = System.currentTimeMillis();
		this.preperationModel = modelTime - logTime;
		double traceFitness = 0;
		Node source = new Node(logAutomaton.sourceID(), modelAutomaton.sourceID(), 
				new Configuration(new IntArrayList(), new IntArrayList(), new ArrayList<Couple<Integer, Integer>>(), new ArrayList<psp.Transition>(), 
						new IntArrayList(), new IntArrayList()), 0);
		source.configuration().logIDs().add(logAutomaton.sourceID()); source.configuration().modelIDs().add(modelAutomaton.sourceID());
		psp = new PSP(HashBiMap.create(), new HashSet<Arc>(), source.hashCode(), logAutomaton, modelAutomaton);
		psp.nodes().put(source.hashCode(), source);
		
		for(int finalState : logAutomaton.finalConfigurations().keySet())
			for(Multiset<Integer> finalConfiguration : logAutomaton.finalConfigurations().get(finalState))
				for(IntArrayList potentialPath : logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).keySet())
					//if(potentialPath.containsAll(0,1,15,16, 92, 151, 119, 117, 108, 5))
						this.calculatePartiallySynchronizedPathWithLeastSkipsFor(finalConfiguration, finalState, potentialPath);
		
		for(AllSyncReplayResult result : replayResult)
			traceFitness = traceFitness += (result.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * result.getTraceIndex().size());
		replayResult.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" +  (traceFitness / xLog.size()));
		long tpsp = System.currentTimeMillis();
		this.timePerformance = tpsp - modelTime;
	}
	
	public ConstructPSP(String path, String logfile, String modelfile, boolean toDot) throws Exception
	{
		long start = System.nanoTime();
		//int i = 1;
		logAutomaton = new ImportEventLog().convertLogToAutomatonFrom(path + "/" + logfile);
		if(toDot)
			logAutomaton.toDot(path + "/" + logfile.substring(0, logfile.length()-4) + ".dot");
		long logTime = System.nanoTime();
		System.out.println("Total log automaton creation: " + TimeUnit.SECONDS.convert((logTime - start), TimeUnit.NANOSECONDS) + "s");
		if(modelfile.endsWith(".pnml"))
			modelAutomaton = new ImportProcessModel().createFSMfromPNMLFile(path + "/" + modelfile, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
		//System.out.println(modelAutomaton.finalConfigurations());
		else
			modelAutomaton = new ImportProcessModel().createFSMfromBPNMFileWithConversion(path + "/" + modelfile, logAutomaton.eventLabels(), logAutomaton.inverseEventLabels());
		if(toDot)
			modelAutomaton.toDot(path + "/" + modelfile.substring(0, modelfile.length()-5) + ".dot");
		long modelTime = System.nanoTime();

		//System.out.println(logAutomaton.eventLabels().values());
		//System.out.println(modelAutomaton.eventLabels().values());
		
		Node source = new Node(logAutomaton.sourceID(), modelAutomaton.sourceID(), 
				new Configuration(new IntArrayList(), new IntArrayList(), new ArrayList<Couple<Integer, Integer>>(), new ArrayList<psp.Transition>(), 
						new IntArrayList(), new IntArrayList()), 0);
		source.configuration().logIDs().add(logAutomaton.sourceID()); source.configuration().modelIDs().add(modelAutomaton.sourceID());
		psp = new PSP(HashBiMap.create(), new HashSet<Arc>(), source.hashCode(), logAutomaton, modelAutomaton);
		psp.nodes().put(source.hashCode(), source);
		//PrintWriter pw = new PrintWriter(path + "/debugging.txt");
		System.out.println("Labels in the log: ");
		System.out.println(logAutomaton.eventLabels());
		System.out.println("Labels in the Model: ");
		System.out.println(modelAutomaton.eventLabels());
		for(int finalState : logAutomaton.finalConfigurations().keySet())
			for(Multiset<Integer> finalConfiguration : logAutomaton.finalConfigurations().get(finalState))
			{
				//pw.println("iteration: " + i + ", Final State: " + finalState + ", final Configuration: " + finalConfiguration);
//				source = new Node(logAutomaton.sourceID(), modelAutomaton.sourceID(), 
//						new Configuration(new IntArrayList(), new IntArrayList(), new ArrayList<Couple<Integer, Integer>>(), new ArrayList<psp.Transition>(), 
//								new IntArrayList(), new IntArrayList()), 0);
//				source.configuration().logIDs().add(logAutomaton.sourceID()); source.configuration().modelIDs().add(modelAutomaton.sourceID());
//				psp = new PSP(HashBiMap.create(), new HashSet<Arc>(), source.hashCode(), logAutomaton, modelAutomaton);
//				psp.nodes().put(source.hashCode(), source);
				for(IntArrayList potentialPath : logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).keySet())
				{
					//pw.println("potential path: " + potentialPath + "; trace labels: " + logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath));
					
					System.out.println("potential path: " + potentialPath + "; trace labels: " + logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath));
					this.calculatePartiallySynchronizedPathWithLeastSkipsFor(finalConfiguration, finalState, potentialPath); //, pw);
				}
//				if(toDot)
//					psp.toDot(path + "/simplePSP" + i + ".dot");
//				i++;
			}
				
		if(toDot)
			psp.toDot(path + "/simplePSP.dot");
		long tpsp = System.nanoTime();
		System.out.println("Automata synchronization: " + TimeUnit.MILLISECONDS.convert((tpsp - modelTime), TimeUnit.NANOSECONDS) + "ms");
		//pw.close();
		//pw.close();
//		System.out.println("Prefix Memorization table:");
//		for(IntArrayList prefix : prefixMemorizationTable.keySet())
//		{
//			System.out.println("Common Prefix: " + prefix);
//			for(Node node : ConstructPSP.prefixMemorizationTable.get(prefix))
//			{
//				System.out.println("Corresponding Node: " + node.stateLogID() + " ; " + node.stateModelID());
//				System.out.println(psp.nodeLabel(node));
//			}
//		}
//		System.out.println("Suffix Memorization table:");
//		for(long states : suffixMemorizationTable.keySet())
//		{
//			System.out.println("Common Suffix: " + ((int) states & 0xFFF) + " - " + (int) ( (states>>32) & 0xFFF));
//			for(Configuration conf : ConstructPSP.suffixMemorizationTable.get(states))
//			{
//				System.out.println(conf.sequenceOperations());
//				System.out.println(conf.moveOnLog());
//				System.out.println(conf.moveOnModel());
//			}
//		}
	}
	
	private void calculatePartiallySynchronizedPathWithLeastSkipsFor(Multiset<Integer> finalConfiguration, int finalState, IntArrayList potentialPath) throws FileNotFoundException //, PrintWriter pw) throws FileNotFoundException
	{
		double start = System.currentTimeMillis();
		//pw.println("Enter method calculatePartiallySynchronizedPathWithLeastSkipsFor(" + finalConfiguration + ", " + finalState + ")");
		Set<Node> visited = new UnifiedSet<Node>();
		Map<Transition, Transition> compEqual = new UnifiedMap<Transition, Transition>();
		PriorityQueue<Node> toBeVisited = new PriorityQueue<>(new NodeComparator());
		Node currentNode = psp.sourceNode();
		toBeVisited.offer(currentNode);
		PriorityQueue<Node> potentialFinalNodes = new PriorityQueue<>(new NodeComparator());
		int actMin = Integer.MAX_VALUE;
		Node potentialNode;
		double numStates = 1;
		
		//this.optimalModelFutures(finalConfiguration);
		
		List<IntArrayList> commonPrefixes= this.traceContainsCommonPrefix(logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath));
		if(!commonPrefixes.isEmpty())
			for(IntArrayList commonPrefix : commonPrefixes)
				for(Node node : this.prefixMemorizationTable.get(commonPrefix))
				{
					potentialNode = this.cloneNodeForConfiguration(node, finalConfiguration, finalState); //, pw);
					toBeVisited.offer(potentialNode);
					visited.add(potentialNode);
				 }
		
		
		
		
		
		while(!toBeVisited.isEmpty())
		{
//			pw.println("-----------------------------------------------");
//			pw.println("Iteration: " + (++i));
			currentNode = toBeVisited.remove();
//			pw.println(currentNode.stateLogID() + " " + currentNode.stateModelID() + " - " + psp.nodeLabel(currentNode));
			numStates++;
			if(suffixMemorizationTable!=null)
			{
				long decodedStates = (long) currentNode.stateLogID() | ((long) currentNode.stateModelID() >> 32);
				Set<Configuration> set;
				if((set = suffixMemorizationTable.get(decodedStates)) != null) 
					for(Configuration suffix : set)
					{
						if(suffix.logIDs().getLast() == finalState)
						{
							potentialNode = this.createPotentialFinalNodeFrom(currentNode, suffix, finalConfiguration, finalState); //, pw);
//							if(!suffix.sequenceOperations().isEmpty())
//							{
//								op = suffix.sequenceOperations().get(0);
//								if(op==Operation.RHIDE)
//									prohibited = new Couple<Configuration.Operation, Integer>(op, suffix.moveOnModel().getFirst());
//								else
//									prohibited = new Couple<Operation, Integer>(op, suffix.moveOnLog().getFirst());
//								prohibitedOperations.add(prohibited);
//							}
							toBeVisited.offer(potentialNode);
							visited.add(potentialNode);
						}
					}
			}
			
			//for (Transition tlog : logAutomaton.states().get(currentNode.stateLogID()).outgoingTransitions())
				
			Transition tmodel = null;
//			pw.println("Potential Nodes:");
			for(Transition tlog : logAutomaton.states().get(currentNode.stateLogID()).outgoingTransitions())
			{	
				if(potentialPath.contains(tlog.target().id()))
				{
					for(Transition tModel : modelAutomaton.states().get(currentNode.stateModelID()).outgoingTransitions())
						if(tlog.eventID()==tModel.eventID() && (!compEqual.values().contains(tModel)))
							compEqual.put(tlog, tModel);
					
					
					if((tmodel = compEqual.get(tlog)) != null)
					{
						potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, finalState, false); //, pw);
						this.offerPotentialNode(potentialNode, actMin, toBeVisited, visited);
						
					}
					potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, finalState, false); //, pw);
					this.offerPotentialNode(potentialNode, actMin, toBeVisited, visited);
				}
			}
			
			for(Transition tModel : modelAutomaton.states().get(currentNode.stateModelID()).outgoingTransitions())
			{
				potentialNode = initializePotentialNode(currentNode, null, tModel, Configuration.Operation.RHIDE, finalConfiguration, finalState, false); //, pw);
				this.offerPotentialNode(potentialNode, actMin, toBeVisited, visited);
			}
			
			compEqual.clear();
			//prohibitedOperations.clear();
			
			if(currentNode.configuration().setMoveOnLog().equals(finalConfiguration) 
					&& currentNode.stateLogID() == finalState 
					&& modelAutomaton.states().get(currentNode.stateModelID()).isFinal()
					&& currentNode.configuration().logIDs().containsAll(potentialPath))
			{
				//System.out.println("final Node found: " + currentNode.stateLogID() + " with weight: " + currentNode.weight());
				potentialFinalNodes.offer(currentNode);
//				pw.println("final Node found: " + currentNode.stateLogID() + " " + currentNode.stateModelID() + psp.nodeLabel(currentNode) + " with weight: " + currentNode.weight());
				actMin = potentialFinalNodes.peek().weight();
//				if(!toBeVisited.isEmpty())
//					pw.println(actMin + " < " + toBeVisited.peek().weight() + ": " + (actMin < toBeVisited.peek().weight()));
			}
			
			if(toBeVisited.isEmpty() && potentialFinalNodes.size()>0) break;
			else 
			if(actMin < toBeVisited.peek().weight()) break;
		}
		//System.out.println(potentialFinalNodes.size());
//		pw.println("-----------------------------------------------");
//		pw.println("final Node: " + currentNode.stateLogID() + " " + currentNode.stateModelID() + psp.nodeLabel(currentNode) + " with weight: " + currentNode.weight());
		//toBeVisited.clear();
		double end = System.currentTimeMillis();
		
		//report results
		List<List<Object>> lstNodeInstanceLst = new ArrayList<List<Object>>();
		List<List<StepTypes>> lstStepTypesLst = new ArrayList<List<StepTypes>>();
		//Integer traceID = null;
//		if(!logAutomaton.caseTracesMapping.containsKey(logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath)))
//		if((traceID = logAutomaton.caseTracesMapping.get(
//						logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath)).getFirst())==null)
//			traceID = -1;
//		else
			//traceID = -1;
		Node potentialFinalNode = null;
		while(true)
		{
			potentialFinalNode = potentialFinalNodes.poll();
			List<Object> nodeInstanceLst = new ArrayList<Object>();
			List<StepTypes> stepTypesLst = new ArrayList<StepTypes>();
			lstNodeInstanceLst.add(nodeInstanceLst);
			lstStepTypesLst.add(stepTypesLst);
			for(psp.Transition tr : potentialFinalNode.configuration().sequenceTransitions())
			{
				if(tr.operation() == Configuration.Operation.MATCH)
				{
					nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
					stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.LMGOOD);
				}
				else if(tr.operation() == Configuration.Operation.LHIDE)
				{
					nodeInstanceLst.add(logAutomaton.eventLabels().get(tr.eventLog()));
					stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.L);
				}
				else
				{
					nodeInstanceLst.add(modelAutomaton.eventLabels().get(tr.eventModel()));
					stepTypesLst.add(org.processmining.plugins.petrinet.replayresult.StepTypes.MREAL);
				}
				
			}
			this.insertPartiallySynchronizedPathIntoPSP(finalConfiguration, finalState, potentialPath, potentialFinalNode); //, pw);
			if(potentialFinalNodes.isEmpty()) break;
		}
		
		AllSyncReplayResult result = new AllSyncReplayResult(lstNodeInstanceLst, lstStepTypesLst, -1, true);
		result.getTraceIndex().remove(-1);
		Integer[] relevantTraces = ArrayUtils.toObject(logAutomaton.caseTracesMapping.get(logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath)).toArray());
		result.getTraceIndex().addAll(Arrays.<Integer>asList( relevantTraces));
		result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) result.getStepTypesLst().size()); 
		result.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, (double) potentialFinalNode.weight());
		result.addInfo(PNMatchInstancesRepResult.NUMSTATES, (double) numStates);
		result.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, (double) numStates + toBeVisited.size());
		result.addInfo(PNMatchInstancesRepResult.ORIGTRACELENGTH, (double) logAutomaton.states().get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath).size());
		result.addInfo(PNMatchInstancesRepResult.TIME, (double) (end - start));
		result.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, (double) lstNodeInstanceLst.size());
		result.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, (double) 1-result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / result.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH));
		
		this.replayResult().add(result);
		this.replayResult().getInfo().replace(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + (Double.parseDouble(this.replayResult().getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS)) + result.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS)));
		this.replayResult().getInfo().replace(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (Double.parseDouble(this.replayResult().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST)) + (result.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) *  result.getTraceIndex().size())));
		this.replayResult().getInfo().replace(PNMatchInstancesRepResult.NUMSTATES, "" + (Double.parseDouble(this.replayResult().getInfo().get(PNMatchInstancesRepResult.NUMSTATES)) + result.getInfo().get(PNMatchInstancesRepResult.NUMSTATES)));
		this.replayResult().getInfo().replace(PNMatchInstancesRepResult.QUEUEDSTATE, "" + (Double.parseDouble(this.replayResult().getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE)) + result.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE)));
		toBeVisited.clear();
	}
	
	private void insertPartiallySynchronizedPathIntoPSP(Multiset<Integer> finalConfiguration, int finalState, IntArrayList potentialPath, Node finalNode) throws FileNotFoundException // , PrintWriter pw) throws FileNotFoundException
	{
		//System.out.println("Enter method insertPartiallySynchronizedPathIntoPSP with configuration: " + finalConfiguration);
		Set<Node> commutativeFinalNodes = null;
		BiMap<IntArrayList, Set<Node>> concurrentPaths = null;
		if((concurrentPaths = psp.concurrentCommutativePaths().get(finalConfiguration)) == null)
		{
			concurrentPaths = HashBiMap.create();
			psp.concurrentCommutativePaths().put(finalConfiguration, concurrentPaths);
		}
		if((commutativeFinalNodes = concurrentPaths.get(potentialPath))==null)
		{
			commutativeFinalNodes = new HashSet<Node>();
			concurrentPaths.put(potentialPath, commutativeFinalNodes);
		}
		commutativeFinalNodes.add(finalNode);
		
		Node currentNode = psp.sourceNode();
		Node potentialNode = currentNode;
		//int indexLog = 0;
		//int indexModel = 0;
		/*
		for(Configuration.Operation operation : finalNode.configuration().sequenceOperations())
			System.out.println(operation);
		*/
		//System.out.println(finalNode.configuration().sequenceOperations());
		for(psp.Transition transition : finalNode.configuration().sequenceTransitions())
		{
			//System.out.println(operation);
			if(transition.operation() == Configuration.Operation.MATCH )
			{
				for(Transition tlog : logAutomaton.states().get(currentNode.stateLogID()).outgoingTransitions())
				{
					for(Transition tmodel : modelAutomaton.states().get(currentNode.stateModelID()).outgoingTransitions())
					{
//						if(tlog.eventID() == finalNode.configuration().moveOnLog().get(indexLog) 
//								&& tmodel.eventID() == finalNode.configuration().moveOnModel().get(indexModel)
//								&& tlog.target().id() == finalNode.configuration().logIDs().get(indexLog+1)
//								&& tmodel.target().id() == finalNode.configuration().modelIDs().get(indexModel+1))
						if(tlog.eventID() == transition.eventLog()
								&& tmodel.eventID() == transition.eventModel()
								&& tlog.target().id() == transition.targetLog()
								&& tmodel.target().id() == transition.targetModel())
						{
							potentialNode = initializePotentialNode(currentNode, tlog, tmodel, Configuration.Operation.MATCH, finalConfiguration, finalState, true); //, pw);
							//System.out.println("Match: " + psp.nodeLabel(potentialNode.hashCode()));
							//System.out.println(logAutomaton.eventLabels().get(tlog.eventID()));
							//indexLog++;
							//indexModel++;
							break;
						}
					}
					if(!(potentialNode.stateLogID() == currentNode.stateLogID()))
						break;
				}
			}
			else if (transition.operation() == Configuration.Operation.LHIDE)
			{
				for(Transition tlog : logAutomaton.states().get(currentNode.stateLogID()).outgoingTransitions())
				{
//					if(tlog.eventID() == finalNode.configuration().moveOnLog().get(indexLog)
//							&& tlog.target().id() == finalNode.configuration().logIDs().get(indexLog+1))
					if(tlog.eventID() == transition.eventLog() && tlog.target().id() == transition.targetLog())
					{
						potentialNode = initializePotentialNode(currentNode, tlog, null, Configuration.Operation.LHIDE, finalConfiguration, finalState, true); //, pw);
						//System.out.println("Lhide: : " + psp.nodeLabel(potentialNode.hashCode()));
						//indexLog++;
						break;
					}
				}
			}
			else
			{
				for(Transition tmodel : modelAutomaton.states().get(currentNode.stateModelID()).outgoingTransitions())
				{
//					if(tmodel.eventID() == finalNode.configuration().moveOnModel().get(indexModel)
//							&& tmodel.target().id() == finalNode.configuration().modelIDs().get(indexModel+1))
					if(tmodel.eventID() == transition.eventModel() && tmodel.target().id() == transition.targetModel())
					{
						potentialNode = initializePotentialNode(currentNode, null, tmodel, Configuration.Operation.RHIDE, finalConfiguration, finalState, true); //, pw);
						//System.out.println("Rhide: " + psp.nodeLabel(potentialNode.hashCode()));
						//System.out.println(modelAutomaton.eventLabels().get(tmodel.eventID()));
						//indexModel++;
						break;
					}
				}
			}
			if(logAutomaton.states().get(potentialNode.stateLogID()).outgoingTransitions().size()>1 && !(potentialNode.stateLogID() == logAutomaton.sourceID()))
			{
				if(prefixMemorizationTable==null)
					prefixMemorizationTable = new UnifiedMap<IntArrayList, Set<Node>>();
				Set<Node> relevantPrefix = null;
				if((relevantPrefix = prefixMemorizationTable.get(potentialNode.configuration().moveOnLog())) == null)
				{
					relevantPrefix = new HashSet<Node>();
					prefixMemorizationTable.put(potentialNode.configuration().moveOnLog(), relevantPrefix);
				}
				relevantPrefix.add(potentialNode);
			}
			if(logAutomaton.states().get(potentialNode.stateLogID()).incomingTransitions().size()>1 && !(potentialNode.stateLogID() == finalState))
			{
				if(suffixMemorizationTable==null)
					suffixMemorizationTable = new UnifiedMap<Long, Set<Configuration>>();
				Set<Configuration> relevantConfiguration = null;
				if((relevantConfiguration = suffixMemorizationTable.get( (long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32)))==null)
				{
					relevantConfiguration = new HashSet<Configuration>();
					suffixMemorizationTable.put( (long) potentialNode.stateLogID() | ((long) potentialNode.stateModelID() << 32), relevantConfiguration);
				}
				relevantConfiguration.add(finalNode.configuration().calculateSuffixFrom(potentialNode.configuration()));
			}
			currentNode = potentialNode;
		}
		
//		if(psp.nodes().containsValue(finalNode))
//		{
		psp.nodes().get(finalNode.hashCode()).isFinal(true);
		psp.finalNodes().add(finalNode.hashCode());
//		}
		//System.out.println("Yes!! Finish!!");
	}
	
	private Node initializePotentialNode(Node currentNode, Transition tlog, Transition tmodel, Configuration.Operation operation, 
			Multiset<Integer> finalConfiguration, int finalState, boolean insertToPSP) //, PrintWriter pw)
	{
		Configuration potentialConfiguration;
		Node potentialNode;
		boolean discover = true;
		if(operation == Configuration.Operation.MATCH)
		{
			potentialConfiguration = currentNode.configuration().cloneConfiguration();
			potentialConfiguration.moveOnLog().add(tlog.eventID());
			potentialConfiguration.moveOnModel().add(tmodel.eventID());
			potentialConfiguration.adjustSetMoveOnLog();
			potentialConfiguration.moveMatching().add(new Couple<Integer, Integer>(tlog.eventID(), tmodel.eventID()));
			potentialConfiguration.sequenceTransitions().add(new psp.Transition(Configuration.Operation.MATCH, tlog.eventID(), tmodel.eventID(), tlog.target().id(), tmodel.target().id(), currentNode.hashCode()));
			potentialConfiguration.logIDs().add(tlog.target().id());
			potentialConfiguration.modelIDs().add(tmodel.target().id());
//			int lastIndex = currentNode.configuration().modelIDs().lastIndexOf(tmodel.target().id());
//			if(lastIndex>0)
//			{
//				IntArrayList testList = new IntArrayList();
//				for(int i=lastIndex; i<potentialConfiguration.moveOnModel().size();i++)
//					testList.add(potentialConfiguration.moveOnModel().get(i));
//				if(testList.average()==modelAutomaton.skipEvent())
//					discover=false;
//			}
			potentialNode = new Node(tlog.target().id(), tmodel.target().id(), potentialConfiguration, 
						calculateCost(potentialConfiguration, finalConfiguration, tlog.target().id(), tmodel.target().id(), finalState), discover);
		}
		else if (operation == Configuration.Operation.LHIDE)
		{
			potentialConfiguration = currentNode.configuration().cloneConfiguration();
			potentialConfiguration.moveOnLog().add(tlog.eventID());
			potentialConfiguration.adjustSetMoveOnLog();
			potentialConfiguration.sequenceTransitions().add(new psp.Transition(Configuration.Operation.LHIDE, tlog.eventID(), -1, tlog.target().id(), -1, currentNode.hashCode()));
			potentialConfiguration.logIDs().add(tlog.target().id());
			potentialNode = new Node(tlog.target().id(), currentNode.stateModelID(), potentialConfiguration, 
						calculateCost(potentialConfiguration, finalConfiguration, tlog.target().id(), 
								currentNode.stateModelID(), finalState));
		} else
		{
			potentialConfiguration = currentNode.configuration().cloneConfiguration();
			potentialConfiguration.moveOnModel().add(tmodel.eventID());
			potentialConfiguration.sequenceTransitions().add(new psp.Transition(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()));
			potentialConfiguration.modelIDs().add(tmodel.target().id());
//			int lastIndex = currentNode.configuration().modelIDs().lastIndexOf(tmodel.target().id());
//			if(lastIndex>0)
//			{
//				IntArrayList testList = new IntArrayList();
//				for(int i=lastIndex; i<potentialConfiguration.moveOnModel().size();i++)
//					testList.add(potentialConfiguration.moveOnModel().get(i));
//				if(testList.average()==modelAutomaton.skipEvent())
//					discover=false;
//			}
			potentialNode = new Node(currentNode.stateLogID(), tmodel.target().id(), potentialConfiguration, 
						calculateCost(potentialConfiguration, finalConfiguration, currentNode.stateLogID(), tmodel.target().id(), finalState), discover);
		}
		
		if(insertToPSP)
		{
			Arc potentialArc;
			if(!psp.nodes().containsValue(potentialNode))
				psp.nodes().put(potentialNode.hashCode(), potentialNode);
			else
				potentialNode = psp.nodes().get(potentialNode.hashCode());
			if(operation == Configuration.Operation.RHIDE)
				potentialArc = new Arc(new psp.Transition(Configuration.Operation.RHIDE, -1, tmodel.eventID(), -1, tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
			else if(operation==Configuration.Operation.LHIDE)
				potentialArc = new Arc(new psp.Transition(operation, tlog.eventID(), -1, tlog.target().id(),-1, currentNode.hashCode()), currentNode, potentialNode);
			else
				potentialArc = new Arc(new psp.Transition(operation, tlog.eventID(), tmodel.eventID(), tlog.target().id(),tmodel.target().id(), currentNode.hashCode()), currentNode, potentialNode);
			if(currentNode.outgoingArcs().add(potentialArc))
				psp.arcs().add(potentialArc);
		}
			
		return potentialNode;
	}
	
	public List<IntArrayList> traceContainsCommonPrefix(IntArrayList traceLabels)
	{
		List<IntArrayList> commonPrefixes = new ArrayList<IntArrayList>();
		if(prefixMemorizationTable==null) return commonPrefixes;
		for(IntArrayList prefix : prefixMemorizationTable.keySet())
		{
			if(prefix.size()>traceLabels.size()) continue;
			if(traceLabels.toString().substring(0, prefix.size()*3-1).equals(prefix.toString().substring(0, prefix.size()*3-1)))
				commonPrefixes.add(prefix);
		}
		return commonPrefixes;
	}
	
	private Node cloneNodeForConfiguration(Node source, Multiset<Integer> finalConfiguration, int finalState) //, PrintWriter pw)
	{
		return new Node(source.stateLogID(), source.stateModelID(), source.configuration().cloneConfiguration(), this.calculateCost(source.configuration(), finalConfiguration, source.stateLogID(), source.stateModelID(), finalState)); //, pw));
	}
	
	private Node createPotentialFinalNodeFrom(Node currentNode, Configuration suffixConfiguration, Multiset<Integer> finalConfiguration, int finalState) //, PrintWriter pw)
	{
		Configuration configuration = currentNode.configuration().cloneConfiguration();
		configuration.addSuffixFrom(suffixConfiguration);
		return new Node(suffixConfiguration.logIDs().getLast(), suffixConfiguration.modelIDs().getLast(), configuration, 
				this.calculateCost(configuration, finalConfiguration, suffixConfiguration.logIDs().getLast(), suffixConfiguration.modelIDs().getLast(), finalState)); //, pw));
	}
	
	private int calculateCost(Configuration configuration, Multiset<Integer> finalConfiguration, int stateLogID, int stateModelID, int finalState) //, PrintWriter pw)//, int stateLogID, int stateModelID, int finalState)
	{
		//pw.println(currentCost(configuration, finalConfiguration, pw) + futureCost(configuration, finalConfiguration, stateLogID, stateModelID, finalState, pw));
		int futureCost = futureCost(configuration, finalConfiguration, stateLogID, stateModelID, finalState); //, pw);
		if(futureCost==Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return currentCost(configuration, finalConfiguration) + futureCost;//, stateLogID, stateModelID, finalState);
	}
	
	private int currentCost(Configuration configuration, Multiset<Integer> finalConfiguration) //, PrintWriter pw)
	{
		IntArrayList moveOnModelWithoutTau = new IntArrayList();
		moveOnModelWithoutTau.addAll(configuration.moveOnModel());
		moveOnModelWithoutTau.removeAll(modelAutomaton.skipEvent());
		Multiset<Integer> finalConfigurationViolations = Multisets.difference(configuration.setMoveOnLog(), finalConfiguration);
		//pw.println(configuration.moveOnLog().size() + " + " + moveOnModelWithoutTau.size() + " - " + "2 * " + configuration.moveMatching().size() + " = " + (configuration.moveOnLog().size() + moveOnModelWithoutTau.size() - 2 * configuration.moveMatching().size()));
		return configuration.moveOnLog().size() + moveOnModelWithoutTau.size() - 2 * configuration.moveMatching().size() + finalConfigurationViolations.size();
	}
	
	
	private int futureCost(Configuration configuration, Multiset<Integer> finalConfigurationLog, int stateLogID, int stateModelID, int finalState) //, PrintWriter pw)//, int stateLogID, int stateModelID, int finalState)
	{
		Multiset<Integer> finalConfigLog = Multisets.difference(finalConfigurationLog, configuration.setMoveOnLog());
		if(stateLogID == finalState && finalConfigLog.isEmpty())
			return 0;
		Set<Multiset<Integer>> logPossibleFutures = null;
		if((logPossibleFutures = logAutomaton.states().get(stateLogID).possibleFutures().get(finalState)) == null) 
			return Math.abs(Integer.MAX_VALUE);
		if(!logPossibleFutures.contains(finalConfigLog))
			return Math.abs(Integer.MAX_VALUE);
		int futureSkips = finalConfigLog.size();
		for(int modfinalState : modelAutomaton.finalStates().toArray())
		{
			Set<Multiset<Integer>> possibleFuturesModel = null;
			if((possibleFuturesModel = modelAutomaton.states().get(stateModelID).possibleFutures().get(modfinalState)) != null)
			{
				for(Multiset<Integer> possibleFutureModel : possibleFuturesModel)
				{
					Multiset<Integer> futureLogSkips = Multisets.difference(finalConfigLog, possibleFutureModel);
					Multiset<Integer> futureModelSkips = HashMultiset.create(possibleFutureModel);
					futureModelSkips.setCount(modelAutomaton.skipEvent(), 0);
					
					Iterator<Integer> it = futureModelSkips.elementSet().iterator();
					while(it.hasNext())
					{
						int element = it.next();
						if(futureModelSkips.count(element) >= 100 && Math.round(futureModelSkips.count(element) / 100) % 2 == 0)
							futureModelSkips.setCount(element, 1 + (futureModelSkips.count(element) % 100));
						else if(futureModelSkips.count(element) >= 100)
						{
							int count = (futureModelSkips.count(element) % 100);
							if(count==0)
								it.remove();
							futureModelSkips.setCount(element, count);
						}
					}
					futureModelSkips = Multisets.difference(futureModelSkips, finalConfigLog);
					//pw.println(futureLog + " - " + possibleFutureModel + " = " + futureLogSkips);
					futureSkips = Math.min(futureSkips, futureLogSkips.size() + futureModelSkips.size());
					if(futureSkips==0) break;
				}
			}
			if(futureSkips==0) break;
		}
		
//		for(int modfinalState : modelAutomaton.finalStates().toArray())
//		{
//			for(Multiset<Integer> optimalModelFuture : this.optimalModelFutures.get(modfinalState))
//			{
//				if(!modelAutomaton.states().get(stateModelID).possibleFutures().get(modfinalState).contains(optimalModelFuture))
//					continue;
//				Multiset<Integer> futureLogSkips = Multisets.difference(finalConfigLog, optimalModelFuture);
//				Multiset<Integer> futureModelSkips = HashMultiset.create(optimalModelFuture);
//				futureModelSkips.setCount(modelAutomaton.skipEvent(), 0);
//				
//				Iterator<Integer> it = futureModelSkips.elementSet().iterator();
//				while(it.hasNext())
//				{
//					int element = it.next();
//					if(futureModelSkips.count(element) >= 100 && Math.round(futureModelSkips.count(element) / 100) % 2 == 0)
//						futureModelSkips.setCount(element, 1 + (futureModelSkips.count(element) % 100));
//					else if(futureModelSkips.count(element) >= 100)
//					{
//						int count = (futureModelSkips.count(element) % 100);
//						if(count==0)
//							it.remove();
//						futureModelSkips.setCount(element, count);
//					}
//				}
//				futureModelSkips = Multisets.difference(futureModelSkips, finalConfigLog);
//				//pw.println(futureLog + " - " + possibleFutureModel + " = " + futureLogSkips);
//				futureSkips = Math.min(futureSkips, futureLogSkips.size() + futureModelSkips.size());
//				if(futureSkips==0) break;
//			}
//			if(futureSkips==0) break;
//		}
		//pw.println(futureSkips);
		return futureSkips;
	}
	
//	private static int futureCost(Configuration configuration, IntHashSet finalConfigurationLog)//, int stateLogID, int stateModelID, int finalState)
//	{
//		IntHashSet finalConfigLog = new IntHashSet(finalConfigurationLog);
//		finalConfigLog.removeAll(configuration.moveOnLog());
//		int futureSkips = finalConfigLog.size();
//		for(Set<IntHashSet> finalConfigurationsModel : modelAutomaton.finalConfigurations().values())
//		{
//			for(IntHashSet finalConfigurationModel : finalConfigurationsModel)
//			{
//				if(finalConfigurationModel.containsAll(configuration.setMoveOnModel()))
//				{
//					IntHashSet futureLog = new IntHashSet(finalConfigLog);
//					IntHashSet futureModel = new IntHashSet(finalConfigurationModel);
//					futureModel.removeAll(configuration.moveOnModel());
//					futureLog.removeAll(futureModel);
//					futureSkips = Math.min(futureSkips, futureLog.size());
//					futureLog.clear();
//					futureModel.clear();
//				}
//			}
//		}
//		return futureSkips;
//	}
	
	private void offerPotentialNode(Node potentialNode, int actMin, Queue<Node> toBeVisited, Set<Node> visited)
	{
		if(!(potentialNode.weight()>actMin && visited.add(potentialNode)))
		{
			toBeVisited.offer(potentialNode);
		}
	}
	
	public PSP psp()
	{
		return this.psp;
	}
	
	public PNMatchInstancesRepResult replayResult()
	{
		if(replayResult==null)
		{
			replayResult = new PNMatchInstancesRepResult(new TreeSet<AllSyncReplayResult>());
			this.replayResult().addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "0.0");
			this.replayResult().addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "0.0");
			this.replayResult().addInfo(PNMatchInstancesRepResult.NUMSTATES, "0.0");
			this.replayResult().addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "0.0");
		}
		return replayResult;
	}
	
	public void optimalModelFutures(Multiset<Integer> finalConfiguration)
	{
		int futureSkips = Integer.MAX_VALUE;
		this.optimalModelFutures = new UnifiedMap<Integer, Set<Multiset<Integer>>>();
		for(int modfinalState : modelAutomaton.finalStates().toArray())
		{
			IntObjectHashMap<Set<Multiset<Integer>>> costToFinalConfigurationMapping = new IntObjectHashMap<Set<Multiset<Integer>>>();
			Set<Multiset<Integer>> possibleFuturesModel = null;
			Set<Multiset<Integer>> sameCostFinalConfigurations = null;
			if((possibleFuturesModel = modelAutomaton.source().possibleFutures().get(modfinalState)) != null)
			{
				for(Multiset<Integer> possibleFutureModel : possibleFuturesModel)
				{
					Multiset<Integer> futureLogSkips = Multisets.difference(finalConfiguration, possibleFutureModel);
					Multiset<Integer> futureModelSkips = HashMultiset.create(possibleFutureModel);
					futureModelSkips.setCount(modelAutomaton.skipEvent(), 0);
					
					Iterator<Integer> it = futureModelSkips.elementSet().iterator();
					while(it.hasNext())
					{
						int element = it.next();
						if(futureModelSkips.count(element) >= 100 && Math.round(futureModelSkips.count(element) / 100) % 2 == 0)
							futureModelSkips.setCount(element, 1 + (futureModelSkips.count(element) % 100));
						else if(futureModelSkips.count(element) >= 100)
						{
							int count = (futureModelSkips.count(element) % 100);
							if(count==0)
								it.remove();
							futureModelSkips.setCount(element, count);
						}
					}
					futureModelSkips = Multisets.difference(futureModelSkips, finalConfiguration);
					//pw.println(futureLog + " - " + possibleFutureModel + " = " + futureLogSkips);
					futureSkips = futureLogSkips.size() + futureModelSkips.size();
					if((sameCostFinalConfigurations = costToFinalConfigurationMapping.get(futureSkips))==null)
					{
						sameCostFinalConfigurations = new UnifiedSet<Multiset<Integer>>();
						costToFinalConfigurationMapping.put(futureSkips, sameCostFinalConfigurations);
					}
					sameCostFinalConfigurations.add(possibleFutureModel);
				}
			}
			this.optimalModelFutures.put(modfinalState,costToFinalConfigurationMapping.get(costToFinalConfigurationMapping.keySet().min()));
			System.out.println(costToFinalConfigurationMapping.keySet().min());
			System.out.println(finalConfiguration);
			System.out.println(this.optimalModelFutures.get(modfinalState));
		}
	}
}
