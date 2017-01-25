package Automaton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

//import ee.ut.mining.log.AlphaRelations;


public class Automaton {

	private Map<Integer, State> states;
	private Map<Integer, String> eventLabels;
	private Map<String, Integer> inverseEventLabels;
	private Map<Integer, Transition> transitions;
	private int source;
	private IntHashSet finalStates;
	//private AlphaRelations concurrencyOracle;
	private Map<Integer, Set<Multiset<Integer>>> finalConfigurations;
	private int skipEvent = -2;
	public Map<IntArrayList, IntArrayList> caseTracesMapping;
	
	public Automaton(Map<Integer, State> states, Map<Integer, String> eventLabels, Map<String, Integer> inverseEventLabels, Map<Integer, Transition> transitions, 
			int initialState, IntHashSet FinalStates, Map<IntArrayList, IntArrayList> caseTracesMapping) throws IOException
	{
		this.states = states;
		this.eventLabels = eventLabels;
		this.inverseEventLabels = inverseEventLabels;
		this.transitions = transitions;
		this.source = initialState;
		this.finalStates = FinalStates;
		this.discoverFinalConfigurations(true);
//		PrintWriter pw = new PrintWriter("/Users/daniel/Documents/workspace/Master thesis paper tests/Road Traffic/tracesContained.txt");
//		for(int finalState : this.finalStates.toArray())
//			for(Multiset<Integer> finalConfiguration : this.finalConfigurations.get(finalState))
//				for(IntArrayList potentialPath : this.states.get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).keySet())
//					if(tracesContained.containsKey(this.states.get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath)))
//						tracesContained.replace(this.states.get(finalState).potentialPathsAndTraceLabels().get(finalConfiguration).get(potentialPath), true);
//		for(IntArrayList trace : tracesContained.keySet())
//				pw.println(trace + " is Contained? " + tracesContained.get(trace));
//		pw.close();
		this.caseTracesMapping = caseTracesMapping;
	}
	
	public Automaton(Map<Integer, State> states, Map<Integer, String> eventLabels, Map<String, Integer> inverseEventLabels, Map<Integer, Transition> transitions,
			int initialState, IntHashSet FinalStates, int skipEvent) throws IOException
	{
		this.states = states;
		this.eventLabels = eventLabels;
		this.inverseEventLabels = inverseEventLabels;
		this.transitions = transitions;
		this.source = initialState;
		this.finalStates = FinalStates;
		this.skipEvent = skipEvent;
		//this.toDot("/Users/daniel/Documents/workspace/Master thesis paper tests/Road Traffic/Traffic fines_loop.dot");
		this.discoverFinalConfigurations(false);
	}
	
//	public Automaton(BiMap<Integer, State> states, BiMap<Integer, String> eventLabels, BiMap<Integer, Transition> transitions, int initialState, Set<Integer> FinalStates, int skipEvent, Map<Integer, Set<IntHashSet>> finalConfigurations) throws FileNotFoundException
//	{
//		this.states = states;
//		this.eventLabels = eventLabels;
//		this.transitions = transitions;
//		this.source = initialState;
//		this.finalStates = FinalStates;
//		this.skipEvent = skipEvent;
//		this.finalConfigurations = finalConfigurations;
//	}
	
	
	public Map<Integer, State> states()
	{
		return this.states;
	}
	
	public Map<Integer, String> eventLabels()
	{
		return this.eventLabels;
	}
	
	public Map<String, Integer> inverseEventLabels()
	{
		return this.inverseEventLabels;
	}
	
	public Map<Integer, Transition> transitions()
	{
		return this.transitions;
	}
	
	public State source()
	{
		return this.states.get(this.source);
	}
	
	public int sourceID()
	{
		return this.source;
	}
	
	public IntHashSet finalStates()
	{
		return this.finalStates;
	}
//	public IntHashSet getFinalStates()
//	{
//		Set<State> finalStates = new HashSet<State>();
//		for(int i : this.finalStates())
//		{
//			finalStates.add(this.states().get(i));
//		}
//		return finalStates;
//	}
	
//	public AlphaRelations concurrencyOracle()
//	{
//		return this.concurrencyOracle;
//	}
	
	public Map<Integer, Set<Multiset<Integer>>> finalConfigurations()
	{
		return this.finalConfigurations;
	}
	
	public int skipEvent()
	{
		return this.skipEvent;
	}
	
//	public void syncEventLabels(BiMap<Integer, String> eventLabels)
//	{
//		for(int key : eventLabels.keySet())
//		{
//			if(this.eventLabels().containsValue(eventLabels.get(key)))
//			{
//				if(this.eventLabels().inverse().get(eventLabels.get(key))==key)
//					continue;
//				if(!this.eventLabels().containsKey(key))
//				{
//					this.eventLabels().remove(this.eventLabels().inverse().get(eventLabels.get(key)));
//					this.eventLabels().put(key, eventLabels.get(key));
//				}
//				else
//				{
//					int oldKey = this.eventLabels().inverse().get(eventLabels.get(key));
//					String replacedValue = this.eventLabels().replace(key, eventLabels.get(key));
//					this.eventLabels().put(oldKey, replacedValue);
//				}
//			}
//		}
//	}
	
	private void discoverFinalConfigurations(boolean discoverPotentialPaths)
	{
		if(!discoverPotentialPaths)
		{
			TarjanSCC scc = new TarjanSCC(this);
			int m = scc.count();
			//System.out.println(m);
			// compute list of vertices in each strong component
	        @SuppressWarnings("unchecked")
			Queue<Integer>[] components = (Queue<Integer>[]) new Queue[m];
	        for (int i = 0; i < m; i++)
	            components[i] = new PriorityQueue<Integer>();
	        
	        for (int v = 1; v < Collections.max(this.states().keySet())+1; v++)
	        {
	            components[scc.id(v)].offer(v);
	            State state = null;
	            if((state = this.states().get(v)) != null)
	            	state.setComponent(scc.id(v));
	        }
	
	        // print results
//	        for (int i = 0; i < m; i++) 
//	        {
//	            for (int v : components[i]) 
//	            	//System.out.print(v + " ");
//	           // System.out.println();
//	        }
	        
	        Map<Integer, ArrayList<Transition>> compIncomingTransitions = new HashMap<Integer, ArrayList<Transition>>();
	        Map<Integer, Multiset<Integer>> loopLabels =new  HashMap<Integer, Multiset<Integer>>();
	        ArrayList<Transition> transitions = null;
	        for(int i = 0; i < m; i++)
	        {
	        	//loops with > 2 nodes?
	        	if(components[i].size()>=2)
	        		for(int state : components[i])
	        		{
	        			State cState = null;
	        			if((cState = this.states().get(state))==null) continue;
	        			for(Transition tr : cState.incomingTransitions())
	        			{
	        				if(!components[i].contains(tr.source().id()))
	        				{
	        					
	        					if((transitions = compIncomingTransitions.get(i)) == null) {
	        					    transitions = new ArrayList<Transition>();
	        						compIncomingTransitions.put(i, transitions);
	        					}
	        					transitions.add(tr);
	        				}
	        				else
	        				{
	        					Multiset<Integer> multiset = null;
	        					if((multiset = loopLabels.get(i)) == null)
	        					{
	        						multiset = HashMultiset.create();
	        						loopLabels.put(i, multiset);
	        					}
	        					multiset.add(tr.eventID());
	        				}
	        			}
	        		}
	        	//selfloops?
	        	else
	        	{
	        		for(Transition tr : this.states().get(components[i].peek()).incomingTransitions())
	        		{
	        			if(tr.source().id() == tr.target().id())
	        			{
	        				Multiset<Integer> multiset = null;
        					if((multiset = loopLabels.get(i)) == null)
        					{
        						multiset = HashMultiset.create();
        						loopLabels.put(i, multiset);
        					}
        					multiset.add(tr.eventID());
        					
//	        				if(!loopLabels.containsKey(i))
//	        					loopLabels.put(i,HashMultiset.create());
//	        				loopLabels.get(i).add(tr.eventID());
	        			}
	        			else
	        			{
	        				
        					if((transitions = compIncomingTransitions.get(i)) == null) {
        					    transitions = new ArrayList<Transition>();
        						compIncomingTransitions.put(i, transitions);
        					}
        					transitions.add(tr);	
	        				
//	        				if(!compIncomingTransitions.containsKey(i))
//	        					compIncomingTransitions.put(i, new IntArrayList());
//	        				compIncomingTransitions.get(i).add(this.transitions().inverse().get(tr));
	        			}
	        		}
	        	}

	        	int count = 200;
	        	Multiset<Integer> multiset = null;
	        	if((multiset = loopLabels.get(i)) != null)
	        	{
	        		if(multiset.contains(this.skipEvent)) 
	        			count=100;
	        		for(int element : multiset.elementSet())
	        			multiset.setCount(element, count);
	        		//System.out.println(loopLabels);
	        	}
	        }
	        
	        IntArrayList toBeVisited = new IntArrayList();
	        Multiset<Integer> possibleFuture;
	        Set<Multiset<Integer>> statePossibleFutures = null;
	        if(this.finalConfigurations == null)
	        	this.finalConfigurations = new HashMap<Integer, Set<Multiset<Integer>>>();
	        finalConfigurations.clear();
	        this.states().values().forEach(state -> state.possibleFutures().clear());
	        
	        for(int finalState : this.finalStates().toArray())
			{
	        	Set<Multiset<Integer>> possibleFutures = null;
	        	if((possibleFutures = this.states().get(finalState).possibleFutures().get(finalState)) == null)
	        	{
	        		possibleFutures = new UnifiedSet<Multiset<Integer>>();
	        		this.states().get(finalState).possibleFutures().put(finalState, possibleFutures);
	        	}
				for(Transition tr : compIncomingTransitions.get(this.states().get(finalState).component()))
				{
					possibleFuture = HashMultiset.create();
					possibleFuture.add(tr.eventID());
					if(loopLabels.containsKey(tr.source().component()))
						possibleFuture.addAll(loopLabels.get(tr.source().component()));
					for(int state : components[tr.source().component()])
					{
						
						if((statePossibleFutures = this.states().get(state).possibleFutures().get(finalState)) == null)
						{
							statePossibleFutures = new UnifiedSet<Multiset<Integer>>();
							this.states().get(state).possibleFutures().put(finalState, statePossibleFutures);
						}
						statePossibleFutures.add(possibleFuture);
					}
					toBeVisited.add(tr.source().id());
				}
				
				while(!toBeVisited.isEmpty())
				{
					int state = toBeVisited.removeAtIndex(0);
					if(compIncomingTransitions.containsKey(this.states().get(state).component()))
					{
						for(Transition tr : compIncomingTransitions.get(this.states().get(state).component()))
						{
							for(Multiset<Integer> sourcePossibleFuture : tr.target().possibleFutures().get(finalState))
							{
								possibleFuture = HashMultiset.create(sourcePossibleFuture);
								possibleFuture.add(tr.eventID());
								if(loopLabels.containsKey(tr.source().component()))
									possibleFuture.addAll(loopLabels.get(tr.source().component()));
								for(int stateComp : components[tr.source().component()])
								{
									Set<Multiset<Integer>> stateCompPossibleFutures = null;
									if((stateCompPossibleFutures = this.states().get(stateComp).possibleFutures().get(finalState)) == null)
									{
										stateCompPossibleFutures = new UnifiedSet<Multiset<Integer>>();
										this.states().get(stateComp).possibleFutures().put(finalState, stateCompPossibleFutures);
									}
									stateCompPossibleFutures.add(possibleFuture);
								}
							}
							if(!toBeVisited.contains(tr.source().id()))
								toBeVisited.add(tr.source().id());
						}
					}	
				}
				this.finalConfigurations().put(finalState, this.source().possibleFutures().get(finalState));
				//System.out.println(finalState + " - " + this.finalConfigurations().get(finalState));
			}
		}
		
		if(discoverPotentialPaths)
		{
			IntArrayList toBeVisited = new IntArrayList();
			//IntHashSet toBeVisited = new IntHashSet();
			boolean exploreState;
			Multiset<Integer> possibleFuture;
			State f_state;
			Set<Multiset<Integer>> possibleFutures = null;
			if(finalConfigurations==null)
				this.finalConfigurations = new HashMap<Integer, Set<Multiset<Integer>>>();
			finalConfigurations.clear();
			this.states().values().forEach(state -> state.possibleFutures().clear());
			
			for(int finalState : this.finalStates().toArray())
			{	
				f_state = this.states().get(finalState); 
				if(!f_state.possibleFutures().containsKey(finalState))
					f_state.possibleFutures().put(finalState, new UnifiedSet<Multiset<Integer>>());
				for(Transition tr : f_state.incomingTransitions())
				{
					possibleFuture = HashMultiset.create(); 
					possibleFuture.add(tr.eventID());
					if((possibleFutures = this.states().get(tr.source().id()).possibleFutures().get(finalState)) == null)
					{
						possibleFutures = new UnifiedSet<Multiset<Integer>>();
						this.states().get(tr.source().id()).possibleFutures().put(finalState, possibleFutures);
					}
					possibleFutures.add(possibleFuture);
					toBeVisited.add(tr.source().id());
				}
				while(!toBeVisited.isEmpty())
				{
					int state = toBeVisited.removeAtIndex(0);
					for(Transition tr : this.states().get(state).incomingTransitions())
					{
						State trSource = tr.source();
						for(Multiset<Integer> futureExtensions : this.states().get(state).possibleFutures().get(finalState))
						{
							possibleFuture = HashMultiset.create(futureExtensions);
							possibleFuture.add(tr.eventID());
							if((possibleFutures = trSource.possibleFutures().get(finalState)) == null)
							{
								possibleFutures = new UnifiedSet<Multiset<Integer>>();
								trSource.possibleFutures().put(finalState, possibleFutures);
							}
							possibleFutures.add(possibleFuture);

							if(!toBeVisited.contains(tr.source().id()))
								toBeVisited.add(tr.source().id());	
						}
					}
				}
				
				finalConfigurations.put(finalState, this.source().possibleFutures().get(finalState));
				
				for(Multiset<Integer> finalConfiguration : this.finalConfigurations().get(finalState))
				{
					Map<Integer, List<IntArrayList>> potentialPaths = new HashMap<Integer, List<IntArrayList>>();
					Map<IntArrayList, Multiset<Integer>> visited = new HashMap<IntArrayList, Multiset<Integer>>();
					IntArrayList newPotentialPath;
					Multiset<Integer> newSetOfLabels = null;
					boolean finalNodeFound = false;
					potentialPaths.put(this.sourceID(), new ArrayList<IntArrayList>());
					potentialPaths.get(this.sourceID()).add(new IntArrayList());
					potentialPaths.get(this.sourceID()).iterator().next().add(this.sourceID());
					visited.put(new IntArrayList(), HashMultiset.create());
					visited.keySet().iterator().next().add(this.sourceID());
					
					for(Transition tr : this.source().outgoingTransitions())
					{
//						exploreState = false;
//						for(IntArrayList potentialPath : potentialPaths.get(tr.source().id()))
//						{
//							newSetOfLabels = HashMultiset.create();
//							newSetOfLabels.add(tr.eventID());
//							if(tr.target().possibleFutures().containsKey(finalState))
//							{
//								for(Multiset<Integer> possibleFutureTarget : tr.target().possibleFutures().get(finalState))
//								{
//									Multiset<Integer> possibleFinalConfiguration = HashMultiset.create(possibleFutureTarget);
//									possibleFinalConfiguration.addAll(newSetOfLabels);
//									if(finalConfiguration.equals(possibleFinalConfiguration))
//										{exploreState = true; break;}
//								}
//							}
//							if(!exploreState) continue;
//							newPotentialPath = new IntArrayList();
//							newPotentialPath.addAll(potentialPath);
//							newPotentialPath.add(tr.target().id());
//							List<IntArrayList> relPotentialPaths = null;
//							if((relPotentialPaths = potentialPaths.get(tr.target().id())) == null)
//							{
//								relPotentialPaths = new ArrayList<IntArrayList>();
//								potentialPaths.put(tr.target().id(), relPotentialPaths);
//							}
//							relPotentialPaths.add(newPotentialPath);
//							
//							visited.put(newPotentialPath, newSetOfLabels);
//							toBeVisited.add(tr.target().id());
//						}
						for(IntArrayList potentialPath : potentialPaths.get(tr.source().id()))
						{
							int targetID = tr.target().id();
							exploreState = false;
							finalNodeFound=false;
							newSetOfLabels = HashMultiset.create();
							newSetOfLabels.add(tr.eventID());
							
							if(targetID==finalState && newSetOfLabels.equals(finalConfiguration))
								{finalNodeFound=true;}
							else if(tr.target().possibleFutures().containsKey(finalState))
							{
								for(Multiset<Integer> possibleFutureTarget : tr.target().possibleFutures().get(finalState))
								{
									Multiset<Integer> possibleFinalConfiguration = HashMultiset.create(possibleFutureTarget);
									possibleFinalConfiguration.addAll(newSetOfLabels);
									if(finalConfiguration.equals(possibleFinalConfiguration))
										{exploreState = true; break;}
								}
							}
							if(!exploreState && !finalNodeFound) continue;
							newPotentialPath = new IntArrayList();
							newPotentialPath.addAll(potentialPath);
							newPotentialPath.add(targetID);
							
							List<IntArrayList> relPotentialPaths = null;
							if((relPotentialPaths = potentialPaths.get(targetID)) == null)
							{
								relPotentialPaths = new ArrayList<IntArrayList>();
								potentialPaths.put(targetID, relPotentialPaths);
							}
							relPotentialPaths.add(newPotentialPath);
//							if(!potentialPaths.containsKey(tr.target().id()))
//								potentialPaths.put(tr.target().id(), new ArrayList<IntArrayList>());
//							potentialPaths.get(tr.target().id()).add(newPotentialPath);
							
							visited.put(newPotentialPath, newSetOfLabels);
							if(!toBeVisited.contains(targetID) && !finalNodeFound)
								toBeVisited.add(targetID);
						}
					}
					
					while(!toBeVisited.isEmpty())
					{
						int state = toBeVisited.removeAtIndex(0);
						if(state == finalState) continue;

						for(Transition tr : this.states().get(state).outgoingTransitions())
						{
							for(IntArrayList potentialPath : potentialPaths.get(tr.source().id()))
							{
								int targetID = tr.target().id();
								exploreState = false;
								finalNodeFound=false;
								newSetOfLabels = HashMultiset.create(visited.get(potentialPath));
								newSetOfLabels.add(tr.eventID());
								
								if(targetID==finalState && newSetOfLabels.equals(finalConfiguration))
									{finalNodeFound=true;}
								else if(tr.target().possibleFutures().containsKey(finalState))
								{
									for(Multiset<Integer> possibleFutureTarget : tr.target().possibleFutures().get(finalState))
									{
										Multiset<Integer> possibleFinalConfiguration = HashMultiset.create(possibleFutureTarget);
										possibleFinalConfiguration.addAll(newSetOfLabels);
										if(finalConfiguration.equals(possibleFinalConfiguration))
											{exploreState = true; break;}
									}
								}
								if(!exploreState && !finalNodeFound) continue;
								newPotentialPath = new IntArrayList();
								newPotentialPath.addAll(potentialPath);
								newPotentialPath.add(targetID);
								
								List<IntArrayList> relPotentialPaths = null;
								if((relPotentialPaths = potentialPaths.get(targetID)) == null)
								{
									relPotentialPaths = new ArrayList<IntArrayList>();
									potentialPaths.put(targetID, relPotentialPaths);
								}
								relPotentialPaths.add(newPotentialPath);
//								if(!potentialPaths.containsKey(tr.target().id()))
//									potentialPaths.put(tr.target().id(), new ArrayList<IntArrayList>());
//								potentialPaths.get(tr.target().id()).add(newPotentialPath);
								
								visited.put(newPotentialPath, newSetOfLabels);
								if(!toBeVisited.contains(targetID) && !finalNodeFound)
									toBeVisited.add(targetID);
							}
						}
					}
					f_state.potentialPathsAndTraceLabels().put(finalConfiguration, new UnifiedMap<IntArrayList, IntArrayList>()); //potentialPaths.get(finalState)
					//System.out.println(finalState +" - " + finalConfiguration + " - " + potentialPaths.get(finalState));
					IntArrayList relevantTraceLabels = null;
					if(!potentialPaths.containsKey(finalState))
						System.out.println("Error!");
					for(IntArrayList potentialPath : potentialPaths.get(finalState))
					{
						relevantTraceLabels = new IntArrayList();
						f_state.potentialPathsAndTraceLabels().get(finalConfiguration).put(potentialPath, relevantTraceLabels);
						Multiset<Integer> testSet = HashMultiset.create(finalConfiguration);
							
						int lastState = -1;
						for(int state : potentialPath.toArray())
						{
							for(Transition tr: this.states().get(state).incomingTransitions())
							{
								if(finalState == 5 && potentialPath.equals(new IntArrayList(0,1,2,3,6,24,25,26,44,128,5)))
									testSet.add(-1);
								if(tr.source().id()==lastState && testSet.contains(tr.eventID()))
								{
									testSet.remove(tr.eventID());
									relevantTraceLabels.add(tr.eventID());
									break;
								}
							}
							lastState = state;
						}
//						if(!caseTracesMapping.containsKey(relevantTraceLabels))
//							System.out.println(relevantTraceLabels);
					}
				}
			}
		}
	}
	
//	private void discoverFinalConfigurations(boolean calculatePotentialPaths) throws FileNotFoundException
//	{
//		//Key is node.id(), value is a set of final Configuration labels. Hence values contains a set of a set of integers, which are event.id()s. 
//		BiMap<Integer, Set<IntHashSet>> visited = HashBiMap.create();
//		IntArrayList toBeVisited = new IntArrayList();
//		IntHashSet newSetOfLabels;
//		
//		//Initialization
//		for(Transition tr : this.source().outgoingTransitions())
//		{
//			newSetOfLabels = new IntHashSet();
//			newSetOfLabels.add(tr.eventID());
//			if(!visited.containsKey(tr.target().id()))
//				visited.put(tr.target().id(), new HashSet<IntHashSet>());
//			visited.get(tr.target().id()).add(newSetOfLabels);
//			toBeVisited.add(tr.target().id());
//		}
//		//Algorithm
//		while(!toBeVisited.isEmpty())
//		{
//			int state = toBeVisited.get(0);
//			toBeVisited.remove(state);
//			for(Transition tr : this.states().get(state).outgoingTransitions())
//			{
//				for(IntHashSet setOfLabels : visited.get(state))
//				{
//					newSetOfLabels = new IntHashSet(setOfLabels);
//					newSetOfLabels.add(tr.eventID());
//					if(!(tr.eventID()==this.skipEvent))
//						if(visited.containsKey(tr.target().id()))
//							if(visited.get(tr.target().id()).contains(newSetOfLabels))
//								continue;
//					if(!visited.containsKey(tr.target().id()))
//						visited.put(tr.target().id(), new HashSet<IntHashSet>());
//					visited.get(tr.target().id()).add(newSetOfLabels);
//					if(!toBeVisited.contains(tr.target().id()))
//						toBeVisited.add(tr.target().id());
//				}
//			}
//		}
//		this.finalConfigurations = new HashMap<Integer, Set<IntHashSet>>();
//		//PrintWriter pw;
//		//if(this.F.size()==1)
//		//	pw = new PrintWriter("/Users/daniel/Documents/workspace/dafsa/modelFinalConfigurations.txt");
//		//else
//		//	pw = new PrintWriter("/Users/daniel/Documents/workspace/dafsa/logFinalConfigurations.txt");
//		
//		for(int finalState : this.finalStates())
//		{	
//			this.finalConfigurations.put(finalState, visited.get(finalState));
//			//pw.println("Final Configurations for node " + finalState + ": ");
//			
////			for(IntHashSet configuration : this.finalConfigurations.get(finalState))
////			{
////				//pw.print("Final Configuration: {" + configuration + "}");
////				System.out.print("Final Configuration: {");
////				
////				for(int event : configuration.toArray())
////					System.out.print(this.E.get(event).label() + ", ");
////				System.out.print("}\n"); 
////			}
//			
//			toBeVisited = new IntArrayList();
//			if(!this.states().get(finalState).possibleFutues().containsKey(finalState))
//				this.states().get(finalState).possibleFutues().put(finalState, new HashSet<IntHashSet>());
//			for(Transition tr : this.states().get(finalState).incomingTransitions())
//			{
//				IntHashSet possibleFuture = new IntHashSet();
//				possibleFuture.add(tr.eventID());
//				if(!this.states().get(tr.source().id()).possibleFutues().containsKey(finalState))
//					this.states().get(tr.source().id()).possibleFutues().put(finalState, new HashSet<IntHashSet>());
//				this.states().get(tr.source().id()).possibleFutues().get(finalState).add(possibleFuture);
//				toBeVisited.add(tr.source().id());
//			}
//			while(!toBeVisited.isEmpty())
//			{
//				int state = toBeVisited.get(0);
//				toBeVisited.remove(state);
//				for(Transition tr : this.states().get(state).incomingTransitions())
//				{
//					for(IntHashSet setOfFutures: this.states().get(state).possibleFutues().get(finalState))
//					{
//						IntHashSet possibleFuture = new IntHashSet(setOfFutures);
//						possibleFuture.add(tr.eventID());
//						if(this.states().get(tr.source().id()).possibleFutues().containsKey(finalState))
//							if(this.states().get(tr.source().id()).possibleFutues().get(finalState).contains(possibleFuture))
//								continue;
//						if(!this.states().get(tr.source().id()).possibleFutues().containsKey(finalState))
//							this.states().get(tr.source().id()).possibleFutues().put(finalState, new HashSet<IntHashSet>());
//						this.states().get(tr.source().id()).possibleFutues().get(finalState).add(possibleFuture);
//						toBeVisited.add(tr.source().id());
//					}
//				}
//			}
//			
//			if(calculatePotentialPaths)
//			{
//				for(IntHashSet finalConfiguration : this.finalConfigurations().get(finalState))
//				{
//					Map<Integer, Set<IntArrayList>> potentialPaths = new HashMap<Integer, Set<IntArrayList>>();
//					IntArrayList newPotentialPath;
//					IntHashSet expectedFuture = new IntHashSet();
//					toBeVisited = new IntArrayList();
//					toBeVisited.add(this.sourceID());
//					potentialPaths.put(this.sourceID(), new HashSet<IntArrayList>());
//					potentialPaths.get(this.sourceID()).add(new IntArrayList());
//					potentialPaths.get(this.sourceID()).iterator().next().add(this.sourceID());
//					boolean validState = false;
//					while(!toBeVisited.isEmpty())
//					{
//						int state = toBeVisited.get(0);
//						toBeVisited.removeAll(state);
//						if(state == finalState) continue;
//						for(Transition tr : this.states().get(state).outgoingTransitions())
//						{
//							validState = false;
//							if(tr.target().id()==finalState && finalConfiguration.contains(tr.eventID())) validState = true;
//							if(finalConfiguration.contains(tr.eventID()))
//								for(Set<IntHashSet> statePossibleFutures : tr.target().possibleFutues().values())
//								{
//									for(IntHashSet statePossibleFuture : statePossibleFutures)
//									{
//										for(IntHashSet stateLabelsVisited : visited.get(tr.target().id()))
//										{
//											expectedFuture.addAll(stateLabelsVisited);
//											expectedFuture.addAll(statePossibleFuture);
//											if(finalConfiguration.containsAll(statePossibleFuture) && finalConfiguration.containsAll(stateLabelsVisited)
//													&& expectedFuture.containsAll(finalConfiguration))
//												{validState = true; expectedFuture.clear(); break;}
//											expectedFuture.clear();
//										}
//										if(validState) break;
//									}
//									if(validState) break;
//								}
//							if(!validState)	continue;
//							for(IntArrayList potentialPath : potentialPaths.get(state))
//							{
//								newPotentialPath = new IntArrayList();
//								newPotentialPath.addAll(potentialPath);
//								newPotentialPath.add(tr.target().id());
//								if(!potentialPaths.containsKey(tr.target().id()))
//									potentialPaths.put(tr.target().id(), new HashSet<IntArrayList>());
//								potentialPaths.get(tr.target().id()).add(newPotentialPath);
//							}
//							if(!toBeVisited.contains(tr.target().id()))
//								toBeVisited.add(tr.target().id());
//						}
//					}
//					this.states().get(finalState).potentialPaths().put(finalConfiguration, potentialPaths.get(finalState));
//					System.out.println(finalConfiguration + " - " + potentialPaths.get(finalState));
//				}
//			}
//		}
//		//pw.close();
//	}
	
	public static boolean isMultisetSubset(Multiset<Integer> testSubset, Multiset<Integer> testSuperset)
	{
		if(testSubset.size() > testSuperset.size() || (!testSubset.containsAll(testSuperset)) || (!testSuperset.containsAll(testSubset)))
			return false;
		boolean isSubset = true;
		for(int element : testSubset.elementSet())
			if(testSubset.count(element)>testSuperset.count(element))
				isSubset=false;
		return isSubset;
	}
	
	public void toDot(PrintWriter pw) throws IOException {
		pw.println("digraph fsm {");
		pw.println("rankdir=LR;");
		pw.println("node [shape=circle,style=filled, fillcolor=white]");
		
		for(State n : this.states.values()) {
			if(n.isSource()) {
				pw.printf("%d [label=\"%s\", fillcolor=\"gray\"];%n", n.id(), n.id());
				//pw.printf("%d [label=\"%d\", fillcolor=\"gray\"];%n", n.id(), n.id());
			} else {
				pw.printf("%d [label=\"%s\"];%n", n.id(), n.id());
				//pw.printf("%d [label=\"%d\"];%n", n.id(), n.id());
			}
			
			for(Transition t : n.outgoingTransitions()) {
				pw.printf("%d -> %d [label=\"%s\"];%n", n.id(), t.target().id(), this.eventLabels().get(t.eventID()));
			}

			if(n.isFinal()) {
				String comment = "";
				/*for(Set<Integer> finalConfiguration: this.finalConfigurations().get(n.id()))
				{
					comment = comment + "<br/>Final Configuration: ";
					for(int event : finalConfiguration)
						comment = comment + this.getEvents().get(event).label() + ", ";
					comment = comment.substring(0, comment.length() -2);
				}*/
				pw.printf("%d [label=<%s%s>, shape=doublecircle];%n", n.id(), n.id(), comment);
				//pw.printf("%d [label=\"%d\", shape=doublecircle];%n", n.id(), n.id());
			}
		}
		pw.println("}");
	}
	
	public void toDot(String fileName) throws IOException {
		PrintWriter pw = new PrintWriter(fileName);
		toDot(pw);
		pw.close();
	}
}
