package importAutomatonFromLogOrModel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramFactory;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.bpmn.Bpmn;
import org.processmining.plugins.bpmn.dialogs.BpmnSelectDiagramDialog;
import org.processmining.plugins.bpmn.parameters.BpmnSelectDiagramParameters;
import org.processmining.plugins.bpmn.plugins.BpmnImportPlugin;
import org.processmining.plugins.petrinet.behavioralanalysis.TSGenerator;
//import org.xmlpull.*;
import org.processmining.plugins.pnml.importing.PnmlImportNet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.raffaeleconforti.context.FakePluginContext;
import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;

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

public class ImportProcessModel 
{
	private BiMap<String, Integer> stateLabelMapping;
	private BiMap<Integer, String> eventLabelMapping;
	private BiMap<String, Integer> inverseEventLabelMapping; 
	private BiMap<Integer, Automaton.State> stateMapping;
	private BiMap<Integer, Automaton.Transition> transitionMapping;
	private IntHashSet finalStates;
	private int iSource = 0;
	private int skipEvent = -2;
	
	public Object[] importPetriNetAndMarking(String fileName) throws Exception
	{
		FakePluginContext context = new FakePluginContext();
		PnmlImportNet imp = new PnmlImportNet();
		return (Object[]) imp.importFile(context, fileName);
	}
	
	public Automaton.Automaton createFSMfromPetrinet(Petrinet pnet, Marking marking, Map<Integer, String> eventLabelMapping, Map<String, Integer> inverseEventLabelMapping) throws ConnectionCannotBeObtained, IOException
	{
		//long start = System.nanoTime();
		Object[] object = new TSGenerator().calculateTS(new FakePluginContext(), pnet, marking);
		ReachabilityGraph pnet_rg = (ReachabilityGraph) object [0];
		Automaton.Automaton model = convertReachabilityGraphToFSM(pnet, pnet_rg, eventLabelMapping, inverseEventLabelMapping);
		//long modelTime = System.nanoTime();
		//System.out.println("Model automaton creation: " + TimeUnit.MILLISECONDS.convert((modelTime - start), TimeUnit.NANOSECONDS) + "ms");
		return model;
	}
	
	public Automaton.Automaton createFSMfromPNMLFile(String fileName, Map<Integer, String> eventLabelMapping, Map<String, Integer> inverseEventLabelMapping) throws Exception
	{
		FakePluginContext context = new FakePluginContext();
		PnmlImportNet imp = new PnmlImportNet();
		Object[] object = (Object[]) imp.importFile(context, fileName);
		Petrinet pnet = (Petrinet) object[0];
		Marking marking = (Marking) object[1];
		long start = System.nanoTime();
		object = new TSGenerator().calculateTS(context, pnet, marking);
		ReachabilityGraph pnet_rg = (ReachabilityGraph) object [0];
		Automaton.Automaton model = convertReachabilityGraphToFSM(pnet, pnet_rg, eventLabelMapping, inverseEventLabelMapping);
		long modelTime = System.nanoTime();
		System.out.println("Model automaton creation: " + TimeUnit.MILLISECONDS.convert((modelTime - start), TimeUnit.NANOSECONDS) + "ms");
		return model;
	}
	
//	public Automaton.Automaton createFSMfromBPNMFile(String fileName, Map<Integer, String> eventLabelMapping, Map<String, Integer> inverseLabelMapping) throws Exception
//	{	
//		FakePluginContext context = new FakePluginContext();
//		Bpmn bpmn = (Bpmn) new BpmnImportPlugin().importFile(context, fileName);
//		long start = System.nanoTime();
//		BpmnSelectDiagramParameters parameters = new BpmnSelectDiagramParameters();
//		@SuppressWarnings("unused")
//		BpmnSelectDiagramDialog dialog = new BpmnSelectDiagramDialog(bpmn.getDiagrams(), parameters);
//		BPMNDiagram newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
//		Map<String, BPMNNode> id2node = new HashMap<String, BPMNNode>();
//		Map<String, Swimlane> id2lane = new HashMap<String, Swimlane>();
//		if (parameters.getDiagram() == BpmnSelectDiagramParameters.NODIAGRAM) {
//			bpmn.unmarshall(newDiagram, id2node, id2lane);
//		} else {
//			Collection<String> elements = parameters.getDiagram().getElements();
//			bpmn.unmarshall(newDiagram, elements, id2node, id2lane);
//		}
//		UnifiedSet<BPMNNode> source = new UnifiedSet<BPMNNode>();
//		for(BPMNNode node : newDiagram.getNodes())
//			if(newDiagram.getInEdges(node).isEmpty()) source.add(node);
//		
//		ArrayList<BPMNNode> toBeVisited = new ArrayList<BPMNNode>();
//		UnifiedSet<BPMNNode> visited = new UnifiedSet<BPMNNode>();
//		BPMNNode curNode = null;
//		
//		this.stateLabelMapping = HashBiMap.create();
//		if(eventLabelMapping.isEmpty())
//			this.eventLabelMapping = HashBiMap.create();
//		else
//			this.eventLabelMapping = HashBiMap.create(eventLabelMapping);
//		if(inverseLabelMapping.isEmpty())
//			this.inverseEventLabelMapping = HashBiMap.create();
//		else
//			this.inverseEventLabelMapping = HashBiMap.create(inverseLabelMapping);
//		this.stateMapping = HashBiMap.create();
//		this.transitionMapping = HashBiMap.create();
//		
//		this.finalStates = new IntHashSet();
//		
//		int iState = 0;
//		int iEvent = this.eventLabelMapping.size();
//		int iTransition = 0;
//		IntHashSet modelEventLabels = new IntHashSet();
//		Integer rkey;
//		
//		this.stateMapping.put(++iState, new Automaton.State(iState, true, false));
//		this.iSource = iState;
//		for(BPMNNode sourceNode : source)
//			if(visited.add(sourceNode))
//				toBeVisited.add(sourceNode);
//		
//		while(!toBeVisited.isEmpty())
//		{
//			curNode = toBeVisited.remove(0);
//			System.out.println("'_' " + curNode.getLabel());
//			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : newDiagram.getOutEdges(curNode))
//			{
//				this.stateMapping.put(++iState, new Automaton.State(iState, false, edge.getGraph().getOutEdges(edge.getTarget()).isEmpty()));
//				this.stateLabelMapping.put("" + edge.getEdgeID(), iState);
//				
//				if(edge.getGraph().getOutEdges(edge.getTarget()).isEmpty()){this.finalStates.add(iState);}
//				
//				if((rkey = this.inverseEventLabelMapping.get(curNode.getLabel())) == null)
//				{
//					iEvent++;
//					rkey = iEvent;
//					this.inverseEventLabelMapping.put(curNode.getLabel(), iEvent);
//					this.eventLabelMapping.put(iEvent, curNode.getLabel());
//					if(curNode.getLabel().isEmpty())
//						skipEvent = iEvent;
//				}
//				modelEventLabels.add(rkey);
//				
//				if(curNode.getGraph().getInEdges(curNode).isEmpty())
//				{
//					this.transitionMapping.put(++iTransition, new Automaton.Transition(this.stateMapping.get(this.iSource), 
//							this.stateMapping.get(this.stateLabelMapping.get("" + edge.getEdgeID())), rkey));
//					this.stateMapping.get(this.iSource).outgoingTransitions().add(this.transitionMapping.get(iTransition));
//					this.stateMapping.get(this.stateLabelMapping.get("" + edge.getEdgeID())).incomingTransitions().add(this.transitionMapping.get(iTransition));
//				}
//				else
//				{
//					for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> inEdge : newDiagram.getInEdges(curNode))
//					{
//						Automaton.State state = null;
//						if((state = this.stateMapping.get( this.stateLabelMapping.get(""+inEdge.getEdgeID()))) == null)
//								System.out.println("Error");
//						this.transitionMapping.put(++iTransition, new Automaton.Transition( state, 
//								this.stateMapping.get(this.stateLabelMapping.get("" + edge.getEdgeID())), rkey));
//						state.outgoingTransitions().add(this.transitionMapping.get(iTransition));
//						this.stateMapping.get(this.stateLabelMapping.get(""+edge.getEdgeID())).incomingTransitions().add(this.transitionMapping.get(iTransition));
//					}
//				}
//				if(edge.getGraph().getInEdges(edge.getTarget()).size()>1)
//				{
//					boolean shouldVisit = true;
//					for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> inEdge : newDiagram.getInEdges(edge.getTarget()))
//						if(!this.stateLabelMapping.containsKey(""+inEdge.getEdgeID())) {shouldVisit = false; break;}
//					if(shouldVisit && visited.add(edge.getTarget()))
//						toBeVisited.add(edge.getTarget());
//				} else 
//				{
//					if(visited.add(edge.getTarget()))
//						toBeVisited.add(edge.getTarget());
//				}
//				
//			}
//		}
//			
//		Set<Integer> keySet = new HashSet<Integer>();
//		keySet.addAll(this.eventLabelMapping.keySet()); 
//		for(int key : keySet)
//			if(!modelEventLabels.contains(key))
//				this.eventLabelMapping.remove(key);
//		this.removeTauTransitions();
//		Automaton.Automaton model = new Automaton.Automaton(this.stateMapping, this.eventLabelMapping, this.inverseEventLabelMapping, this.transitionMapping, iSource, this.finalStates, skipEvent);//, ImportPetriNet.readFile());
//		long modelTime = System.nanoTime();
//		System.out.println("Model automaton creation: " + TimeUnit.MILLISECONDS.convert((modelTime - start), TimeUnit.NANOSECONDS) + "ms");
//		return model;
//	}
	
	public Automaton.Automaton createFSMfromBPNMFileWithConversion(String fileName, Map<Integer, String> eventLabelMapping, Map<String, Integer> inverseLabelMapping) throws Exception
	{	
		FakePluginContext context = new FakePluginContext();
		Bpmn bpmn = (Bpmn) new BpmnImportPlugin().importFile(context, fileName);
		long start = System.nanoTime();
		BpmnSelectDiagramParameters parameters = new BpmnSelectDiagramParameters();
		@SuppressWarnings("unused")
		BpmnSelectDiagramDialog dialog = new BpmnSelectDiagramDialog(bpmn.getDiagrams(), parameters);
		BPMNDiagram newDiagram = BPMNDiagramFactory.newBPMNDiagram("");
		Map<String, BPMNNode> id2node = new HashMap<String, BPMNNode>();
		Map<String, Swimlane> id2lane = new HashMap<String, Swimlane>();
		if (parameters.getDiagram() == BpmnSelectDiagramParameters.NODIAGRAM) {
			bpmn.unmarshall(newDiagram, id2node, id2lane);
		} else {
			Collection<String> elements = parameters.getDiagram().getElements();
			bpmn.unmarshall(newDiagram, elements, id2node, id2lane);
		}
		Object[] object = BPMNToPetriNetConverter.convert(newDiagram);
		Petrinet pnet = (Petrinet) object[0];
		
		int count = 1;
		for(Place p : pnet.getPlaces()) {
			if(p.getLabel().isEmpty()) {
				p.getAttributeMap().put(AttributeMap.LABEL, "p" + count++);
			}
		}
		
		Marking initialMarking = (Marking) object[1];
		context.addConnection(new InitialMarkingConnection(pnet, initialMarking));
		context.addConnection(new FinalMarkingConnection(pnet, (Marking) object[2]));
		object = new TSGenerator().calculateTS(context, pnet, initialMarking);
		ReachabilityGraph pnet_rg = (ReachabilityGraph) object [0];
		Automaton.Automaton model = convertReachabilityGraphToFSM(pnet, pnet_rg, eventLabelMapping, inverseLabelMapping);
		long modelTime = System.nanoTime();
		System.out.println("Model automaton creation: " + TimeUnit.MILLISECONDS.convert((modelTime - start), TimeUnit.NANOSECONDS) + "ms");
		return model;
		
	}
	
	public Automaton.Automaton convertReachabilityGraphToFSM(Petrinet pnet, ReachabilityGraph pnet_rg, Map<Integer, String> eventLabels, Map<String, Integer> inverseEventLabels) throws IOException
	{
		
		this.stateLabelMapping = HashBiMap.create();
		if(eventLabels.isEmpty())
			this.eventLabelMapping = HashBiMap.create();
		else
			this.eventLabelMapping = HashBiMap.create(eventLabels);
		if(inverseEventLabels.isEmpty())
			this.inverseEventLabelMapping = HashBiMap.create();
		else
			this.inverseEventLabelMapping = HashBiMap.create(inverseEventLabels);
		this.stateMapping = HashBiMap.create();
		this.transitionMapping = HashBiMap.create();
		
		this.finalStates = new IntHashSet();
		
		int iState = 0;
		int iEvent = this.eventLabelMapping.size();
		int iTransition = 0;
		IntHashSet modelEventLabels = new IntHashSet();
		Integer rkey;
		
		for (State s : pnet_rg.getNodes())
		{
			if(!this.stateMapping.containsKey(this.stateLabelMapping.get(s.getLabel())))
			{
				iState++;
				this.stateMapping.put(iState, new Automaton.State(iState, s.getLabel(), s.getGraph().getInEdges(s).isEmpty(), s.getGraph().getOutEdges(s).isEmpty()));
				this.stateLabelMapping.put(s.getLabel(), iState);
				if(s.getGraph().getInEdges(s).isEmpty() && iSource==0){iSource=iState;}
				if(s.getGraph().getOutEdges(s).isEmpty()){this.finalStates.add(iState);}
			}
			
			for(Transition t : s.getGraph().getOutEdges(s))
			{
				if(!this.stateMapping.containsKey(this.stateLabelMapping.get(t.getTarget().getLabel())))
				{
					iState++;
					this.stateMapping.put(iState, new Automaton.State(iState, t.getTarget().getLabel(), 
							t.getTarget().getGraph().getInEdges(t.getTarget()).isEmpty(), 
							t.getTarget().getGraph().getOutEdges(t.getTarget()).isEmpty()));
					this.stateLabelMapping.put(t.getTarget().getLabel(), iState);
					if(t.getTarget().getGraph().getInEdges(t.getTarget()).isEmpty() && iSource==0){iSource=iState;}
					if(t.getTarget().getGraph().getOutEdges(t.getTarget()).isEmpty()){this.finalStates.add(iState);}
				}
				
				for(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition : pnet.getTransitions()) 
				{
					if(transition.isInvisible())
					{
						if(t.getLabel().equals(transition.getLabel()))
						{
							t.setLabel("tau");
							break;
						}
					}
				}
				
				if(t.getLabel().contains("Tau") || t.getLabel().contains("tau") || t.getLabel().contains("invisible") || t.getLabel()=="" ||
						t.getLabel().matches("(T|t)(\\d+)"))
					t.setLabel("tau");
				
				if((rkey = this.inverseEventLabelMapping.get(t.getLabel())) == null)
				{
					iEvent++;
					rkey = iEvent;
					this.inverseEventLabelMapping.put(t.getLabel(), iEvent);
					this.eventLabelMapping.put(iEvent, t.getLabel());
					if(t.getLabel().equals("tau"))
						skipEvent = iEvent;
				}
				modelEventLabels.add(rkey);
				
				iTransition++;
				this.transitionMapping.put( iTransition, new Automaton.Transition(this.stateMapping.get(this.stateLabelMapping.get(t.getSource().getLabel())), 
						this.stateMapping.get(this.stateLabelMapping.get(t.getTarget().getLabel())), rkey));
				
				this.stateMapping.get(this.stateLabelMapping.get(s.getLabel())).outgoingTransitions().add(this.transitionMapping.get(iTransition));
				this.stateMapping.get(this.stateLabelMapping.get(t.getTarget().getLabel())).incomingTransitions().add(this.transitionMapping.get(iTransition));
			}	
		}
		Set<Integer> keySet = new HashSet<Integer>();
		keySet.addAll(this.eventLabelMapping.keySet()); 
		for(int key : keySet)
			if(!modelEventLabels.contains(key))
				this.eventLabelMapping.remove(key);
		this.removeTauTransitions();
		return new Automaton.Automaton(this.stateMapping, this.eventLabelMapping, this.inverseEventLabelMapping, this.transitionMapping, iSource, this.finalStates, skipEvent);//, ImportPetriNet.readFile());
	}
	
	public void removeTauTransitions()
	{
		IntArrayList toBeVisited = new IntArrayList();
		IntHashSet visited = new IntHashSet();
		
		for(int finalState : this.finalStates.toArray())
		{
			Automaton.State fState = this.stateMapping.get(finalState);
			Iterator<Automaton.Transition> it = fState.incomingTransitions().iterator();
			while(it.hasNext())
			{
				Automaton.Transition tr = it.next();
//				if(tr.eventID()==skipEvent)
//				{
//					for(Automaton.Transition repTr : tr.target().outgoingTransitions())
//					{
//						if(repTr.eventID()==skipEvent) continue;
//						Automaton.Transition newTr = new Automaton.Transition(tr.source(), repTr.target(), repTr.eventID());
//						tr.source().outgoingTransitions().add(newTr);
//						repTr.target().incomingTransitions().add(newTr);
//						this.transitionMapping.put(transitionMapping.keySet().size(), newTr);
//					}
//					tr.source().outgoingTransitions().remove(tr);
//					//tr.target().incomingTransitions().remove(tr);
//					it.remove();
//					this.transitionMapping.remove(tr);
//				}
				if(visited.add(tr.source().id()))
					toBeVisited.add(tr.source().id());
			}
		}
		
		while(!toBeVisited.isEmpty())
		{
			Automaton.State state = this.stateMapping.get(toBeVisited.removeAtIndex(0));
			Iterator<Automaton.Transition> it = state.incomingTransitions().iterator();
			while(it.hasNext())
			{
				Automaton.Transition tr = it.next();
				if(tr.eventID()==skipEvent)
				{
					for(Automaton.Transition repTr : tr.target().outgoingTransitions())
					{
						if(repTr.eventID()==skipEvent) continue;
						Automaton.Transition newTr = new Automaton.Transition(tr.source(), repTr.target(), repTr.eventID());
						tr.source().outgoingTransitions().add(newTr);
						repTr.target().incomingTransitions().add(newTr);
						this.transitionMapping.put(transitionMapping.keySet().size(), newTr);
					}
					tr.source().outgoingTransitions().remove(tr);
					//tr.target().incomingTransitions().remove(tr);
					it.remove();
					this.transitionMapping.remove(tr);
				}
				if(visited.add(tr.source().id()))
					toBeVisited.add(tr.source().id());
			}
		}
		
		Iterator<Automaton.State> it = this.stateMapping.values().iterator();
		while(it.hasNext())
		{
			Automaton.State state = it.next();
			if(state.incomingTransitions().isEmpty() && !state.isSource())
			{
				for(Automaton.Transition tr : state.outgoingTransitions())
				{
					tr.target().incomingTransitions().remove(tr);
					this.transitionMapping.remove(tr);
				}
				it.remove();
				//this.stateMapping.remove(state);
			}
		}
	}
	
//	private static Map<Integer, Set<IntHashSet>> readFile() {
//		
//		String fileName = "/Users/daniel/Documents/workspace/dafsa/modelFinalConfigurations.txt";
//        Map<Integer, Set<IntHashSet>> finalConfigurations = new HashMap<>();
//        finalConfigurations.put(29, new HashSet<IntHashSet>());
//
//        try (
//            FileInputStream fis = new FileInputStream(fileName);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(fis))
//        ) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] args = line.split(",");
//                IntHashSet finalConfiguration = new IntHashSet();
//                for(String arg : args)
//                {
//                	finalConfiguration.add(Integer.parseInt(arg));
//                }
//                finalConfigurations.get(29).add(finalConfiguration);
//                
//            }
//        } catch (IOException e) {
//            System.out.printf("Problem loading: %s %n", fileName);
//            e.printStackTrace();
//        }
//
//        return finalConfigurations;
//    }  
	
}
