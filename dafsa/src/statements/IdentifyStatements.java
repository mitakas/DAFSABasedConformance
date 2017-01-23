package statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import com.google.common.collect.Multiset;

import psp.Configuration;
import psp.Node;
import psp.PSP;
import psp.Transition;

public class IdentifyStatements {
	private PSP psp;
	private List<String> statements;
	
	public IdentifyStatements(PSP psp)
	{
		this.psp = psp;
		for(int finalStateLog : psp.logAutomaton().finalStates().toArray())
		{
			for(Multiset<Integer> finalConfiguration : this.psp.logAutomaton().finalConfigurations().get(finalStateLog))
			{
				for(IntArrayList potentialPath : psp.logAutomaton().states().get(finalStateLog).potentialPathsAndTraceLabels().get(finalConfiguration).keySet())
				{
					for(Node finalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(potentialPath))
					{
						for(Transition tr : finalNode.configuration().sequenceTransitions())
						{
							if(tr.operation() != Configuration.Operation.MATCH && tr.eventModel() != psp.modelAutomaton().skipEvent()) // && !tr.isExplained())
							{
								discoverCausConcOrTaskRelocationMismatch(tr, finalNode, finalConfiguration, potentialPath);
								if(tr.isExplained()) continue;
								discoverConflictOrTaskSkipMismatch(tr, finalNode, finalConfiguration, potentialPath);
								if(tr.isExplained()) continue;
								discoverUnmatchedRepetition(tr, finalNode, finalConfiguration, potentialPath);
								if(tr.isExplained() || tr.isTransitivelyExplained()) continue;
								discoverTaskSubstitution(tr, finalNode, finalConfiguration, potentialPath);
								if(tr.isExplained() || tr.isTransitivelyExplained()) continue;
								assertTaskAbsenceInsertion(tr, finalNode, finalConfiguration, potentialPath);
							}
						}
					}
				}
			}
		}
	}
	
	public List<String> statements()
	{
		if(this.statements==null)
			this.statements = new ArrayList<String>();
		return this.statements;
	}
	
	private void discoverCausConcOrTaskRelocationMismatch(Transition tr, Node finalNode, Multiset<Integer> finalConfiguration, IntArrayList potentialPath)
	{
		//Test CausConc mismatch
		Transition testTr;
		Transition testTr2;
		Transition testTr3;
		Transition testTr4;
		int indexTestTr;
		int indexTestTr3;
		boolean concurrencyMismatchFound = false;
		String statement = "";
		
		if(tr.operation()==Configuration.Operation.LHIDE)
			testTr = new Transition(Configuration.Operation.RHIDE, -1, tr.eventLog());
		else
			testTr = new Transition(Configuration.Operation.LHIDE, tr.eventModel(), -1);
		
		if((indexTestTr = finalNode.configuration().sequenceTransitions().indexOf(testTr)) != -1)
		{
			testTr = finalNode.configuration().sequenceTransitions().get(indexTestTr);
			if(!finalNode.configuration().sequenceTransitions().get(indexTestTr).isExplained())
			{
				//System.out.println(finalNode.configuration().sequenceTransitions().indexOf(tr) + " " + indexTestTr);
				if(tr.operation()==Configuration.Operation.LHIDE)
					testTr2 = new Transition(Configuration.Operation.MATCH, tr.eventLog(), tr.eventLog());
				else
					testTr2 = new Transition(Configuration.Operation.MATCH, tr.eventModel(), tr.eventModel());
				
				for(Node commutativeFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(potentialPath))
				{
					//System.out.println(commutativeFinalNode.equals(finalNode));
					if(commutativeFinalNode.equals(finalNode))
						continue;
					if(commutativeFinalNode.configuration().sequenceTransitions().contains(testTr2))
					{
						for(Transition tr2 : commutativeFinalNode.configuration().sequenceTransitions())
						{
							if(!tr2.isExplained() && tr2.operation() != Configuration.Operation.MATCH && tr2.eventModel() != psp.modelAutomaton().skipEvent())
							{
								if(tr2.operation()==Configuration.Operation.LHIDE)
									testTr3 = new Transition(Configuration.Operation.RHIDE, -1, tr2.eventLog());
								else
									testTr3 = new Transition(Configuration.Operation.LHIDE, tr2.eventModel(), -1);
								if((indexTestTr3 = commutativeFinalNode.configuration().sequenceTransitions().indexOf(testTr3)) != -1)
								{
									if(tr2.operation()==Configuration.Operation.LHIDE)
										testTr4 = new Transition(Configuration.Operation.MATCH, tr2.eventLog(), tr2.eventLog());
									else
										testTr4 = new Transition(Configuration.Operation.MATCH, tr2.eventModel(), tr2.eventModel());
									//System.out.println(commutativeFinalNode.configuration().sequenceTransitions().indexOf(tr2) + " " + indexTestTr2);
									for(IntArrayList concurrentPath : psp.concurrentCommutativePaths().get(finalConfiguration).keySet())
									{
										if(concurrentPath.equals(potentialPath)) continue;
										for(Node commutativeConcurrentFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(concurrentPath))
										{
											if(commutativeConcurrentFinalNode.configuration().sequenceTransitions().contains(testTr2)
											&& commutativeConcurrentFinalNode.configuration().sequenceTransitions().contains(testTr4))
											{concurrencyMismatchFound = true; break;}
										}
									}
									if(concurrencyMismatchFound)
									{
										//assert CausConc mismatch
										tr.setExplainedTo(true);
										finalNode.configuration().sequenceTransitions().get(indexTestTr).setTransitivelyExplainedTo(true);
										tr2.setTransitivelyExplainedTo(true);
										commutativeFinalNode.configuration().sequenceTransitions().get(indexTestTr3).setTransitivelyExplainedTo(true);
										if(tr.operation()==Configuration.Operation.RHIDE && tr2.operation()==Configuration.Operation.RHIDE)
											statement = ("In the model, after the Marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() + "\""
													+ ", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" occurs before \"" + psp.logAutomaton().eventLabels().get(testTr3.eventLog())
													+ "\", while in the log they are concurrent");
										else if(tr.operation()==Configuration.Operation.RHIDE && tr2.operation()==Configuration.Operation.LHIDE)
											statement = ("In the model, after the Marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() + "\""
													+ ", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" occurs before \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog())
													+ "\", while in the log they are concurrent");
										else if(tr.operation()==Configuration.Operation.LHIDE && tr2.operation()==Configuration.Operation.RHIDE)
											statement = ("In the model, after the Marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() + "\""
													+ ", \"" + psp.modelAutomaton().eventLabels().get(testTr.eventModel()) + "\" occurs before \"" + psp.logAutomaton().eventLabels().get(testTr3.eventLog())
													+ "\", while in the log they are concurrent");
									}
//									else
//									{
//										//assert Task relocation mismatch"
//										tr.setExplainedTo(true);
//										if(testTr.operation()==Configuration.Operation.RHIDE)
//											statement = "In the model, \"" + psp.modelAutomaton().eventLabels().get(testTr.eventModel()) + "\" occurs after \"" 
//												+ psp.modelAutomaton().eventLabels().get(psp.nodes().get(testTr.sourceNode()).configuration().moveOnModel().getLast()) + "\" instead of \""
//												+ psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast() + "\"");
//										else
//											statement = "In the log, \"" + psp.logAutomaton().eventLabels().get(testTr.eventLog()) + "\" occurs after \"" 
//												+ psp.logAutomaton().eventLabels().get(psp.nodes().get(testTr.sourceNode()).configuration().moveOnLog().getLast()) + "\" instead of \""
//												+ psp.modelAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnModel().getLast() + "\"");
//									}
									
								}
							}
						}
					}
				}
				if(!concurrencyMismatchFound)
				{
					//System.out.println("assert Task relocation mismatch");
					tr.setExplainedTo(true);
					finalNode.configuration().sequenceTransitions().get(indexTestTr).setTransitivelyExplainedTo(true);
					if(testTr.operation()==Configuration.Operation.RHIDE)
						statement = "In the model, \"" + psp.modelAutomaton().eventLabels().get(testTr.eventModel()) + "\" occurs after \"" 
							+ psp.modelAutomaton().eventLabels().get(psp.nodes().get(testTr.sourceNode()).configuration().moveOnModel().getLast()) + "\" instead of \""
							+ psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast()) + "\"";
					else
						statement = "In the log, \"" + psp.logAutomaton().eventLabels().get(testTr.eventLog()) + "\" occurs after \"" 
							+ psp.logAutomaton().eventLabels().get(psp.nodes().get(testTr.sourceNode()).configuration().moveOnLog().getLast()) + "\" instead of \""
							+ psp.modelAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnModel().getLast()) + "\"";
				}
			}
			
		}
		if(!statement.isEmpty() && !this.statements().contains(statement))
			this.statements().add(statement);
	}
	
	private void discoverConflictOrTaskSkipMismatch(Transition tr, Node finalNode, Multiset<Integer> finalConfiguration, IntArrayList potentialPath)
	{
		Transition testTr;
		Transition testTr2;
		//int indexTestTr;
		boolean firstCriteriaCovered = false;
		boolean secondCriteriaCovered = false;
		//1. Hide e1 and Match e2 are found in another path
		//2. Match e1 and Hide e2 are also found in another path
		Node nodeFirstCriteria = null;
		Node nodeSecondCriteria = null;
		int indexFirstCriteria = -1;
		int indexSecondCriteria = -1;
		boolean conflictFound = false;
		String statement = "";
		
		if(tr.operation()==Configuration.Operation.LHIDE)
			testTr = new Transition(Configuration.Operation.MATCH, tr.eventLog(), tr.eventLog());
		else
			testTr = new Transition(Configuration.Operation.MATCH, tr.eventModel(), tr.eventModel());
		
		//Different Manifestation for Conflict in Log and Conflict in Model
		//First Conflict in Log
		if(tr.operation()==Configuration.Operation.LHIDE)
		{
			for(Node commutativeFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(potentialPath))
			{
				if(commutativeFinalNode.equals(finalNode))
					continue;
				if(commutativeFinalNode.configuration().sequenceTransitions().contains(testTr))
				{
					for(Transition tr2 : commutativeFinalNode.configuration().sequenceTransitions())
					{
						if(!tr2.isExplained() && tr2.operation() == tr.operation() && tr2.eventModel() != psp.modelAutomaton().skipEvent())
						{
							testTr2 = new Transition(Configuration.Operation.MATCH, tr2.eventLog(), tr2.eventLog());
							if(finalNode.configuration().sequenceTransitions().contains(testTr2))
							{
								//found Conflict Mismatch
								//Test, if it's causal or Concurrent error
								for(IntArrayList concurrentPath : psp.concurrentCommutativePaths().get(finalConfiguration).keySet())
								{
									if(concurrentPath.equals(potentialPath)) continue;
									for(Node commutativeConcurrentFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(concurrentPath))
									{
										
										if(commutativeConcurrentFinalNode.configuration().sequenceTransitions().contains(tr)
											&& commutativeConcurrentFinalNode.configuration().sequenceTransitions().contains(testTr2))
											{
												firstCriteriaCovered = true; 
												nodeFirstCriteria = commutativeConcurrentFinalNode;
												indexFirstCriteria = commutativeConcurrentFinalNode.configuration().sequenceTransitions().indexOf(tr);
											}
										else if((indexSecondCriteria = commutativeConcurrentFinalNode.configuration().sequenceTransitions().indexOf(tr2)) != -1
													&& commutativeConcurrentFinalNode.configuration().sequenceTransitions().contains(testTr))
														{secondCriteriaCovered = true; nodeSecondCriteria = commutativeConcurrentFinalNode;}
													if(firstCriteriaCovered && secondCriteriaCovered) break;
										
									}
									if(firstCriteriaCovered && secondCriteriaCovered) break;
								}
								if(firstCriteriaCovered && secondCriteriaCovered)// && !nodeFirstCriteria.configuration().sequenceTransitions().get(indexFirstCriteria).isTransitivelyExplained())
								{
									//assert Log concurrent Model Conflict Mismatch
									if(nodeFirstCriteria.configuration().sequenceTransitions().get(indexFirstCriteria).isTransitivelyExplained()) continue;
									tr.setExplainedTo(true);
									tr.setTransitivelyExplainedTo(true);
									tr2.setTransitivelyExplainedTo(true);
									nodeFirstCriteria.configuration().sequenceTransitions().get(indexFirstCriteria).setTransitivelyExplainedTo(true);
									nodeSecondCriteria.configuration().sequenceTransitions().get(indexSecondCriteria).setTransitivelyExplainedTo(true);
									if(finalNode.configuration().sequenceTransitions().indexOf(tr) < commutativeFinalNode.configuration().sequenceTransitions().indexOf(tr2))
										statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast())
											+ "\", \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\" and \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog())
											+ "\" are concurrent, while in the model they are mutually exclusive";
									else
										statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr2.sourceNode()).configuration().moveOnLog().getLast())
												+ "\", \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog()) + "\" and \"" + psp.logAutomaton().eventLabels().get(tr.eventLog())
												+ "\" are concurrent, while in the model they are mutually exclusive";
								}
								else if(
										psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).outgoingTransitions().size() >= 2
									 || psp.modelAutomaton().states().get(psp.nodes().get(tr2.sourceNode()).stateModelID()).outgoingTransitions().size() >= 2
										)
								{
									//assert Log sequential Model Conflict Mismatch
									tr.setExplainedTo(true);
									tr2.setTransitivelyExplainedTo(true);
									if(finalNode.configuration().sequenceTransitions().indexOf(tr) < commutativeFinalNode.configuration().sequenceTransitions().indexOf(tr2))
										statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast())
											+ "\", \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\" occurs before task \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog())
											+ "\", while in the model they are mutually exclusive after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() + "\"";
									else
										statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr2.sourceNode()).configuration().moveOnLog().getLast())
											+ "\", \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog()) + "\" occurs before task \"" + psp.logAutomaton().eventLabels().get(tr.eventLog())
											+ "\", while in the model they are mutually exclusive after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr2.sourceNode()).stateModelID()).label() + "\"";
								}
							}
						}
					}
				}
			}
		}
		else
		{
			for(Multiset<Integer> otherFinalConfiguration : psp.concurrentCommutativePaths().keySet())
			{
				if(otherFinalConfiguration.equals(finalConfiguration)) continue;
				for(Set<Node> commutativeConflictingFinalNodes : psp.concurrentCommutativePaths().get(otherFinalConfiguration).values())
				{
					for(Node commutativeConflictingFinalNode : commutativeConflictingFinalNodes)
					{
						if((indexFirstCriteria = commutativeConflictingFinalNode.configuration().sequenceTransitions().indexOf(testTr)) != -1)
						{
							for(Transition tr2 : commutativeConflictingFinalNode.configuration().sequenceTransitions())
							{
								firstCriteriaCovered = false;
								if(!tr2.isExplained() && tr2.operation() == tr.operation() && tr2.eventModel() != psp.modelAutomaton().skipEvent())
								{
									testTr2 = new Transition(Configuration.Operation.MATCH, tr2.eventModel(), tr2.eventModel());
									if(finalNode.configuration().sequenceTransitions().contains(testTr2))
									{
										//found Conflict Mismatch
										//Test, if it's causal or Concurrent error
										for(Node commutativeFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(potentialPath))
										{
											if(commutativeFinalNode.equals(finalNode)) continue;
											if((indexFirstCriteria = commutativeFinalNode.configuration().sequenceTransitions().indexOf(tr)) != -1 
												&& (indexSecondCriteria = commutativeFinalNode.configuration().sequenceTransitions().indexOf(testTr2)) != -1)
												{firstCriteriaCovered = true; nodeFirstCriteria = commutativeFinalNode; break;}
//											else if((indexFirstCriteria = commutativeFinalNode.configuration().sequenceTransitions().indexOf(tr)) != -1 
//													&& commutativeFinalNode.configuration().sequenceTransitions().contains(testTr2))
//												{secondCriteriaCovered = true; nodeSecondCriteria = commutativeFinalNode;}
//											if(firstCriteriaCovered && secondCriteriaCovered) break;
										}
										
										if(firstCriteriaCovered)
										{
											//assert Log Conflict Model Concurrent Mismatch
											tr.setExplainedTo(true);
											tr2.setTransitivelyExplainedTo(true);
											nodeFirstCriteria.configuration().sequenceTransitions().get(indexFirstCriteria).setTransitivelyExplainedTo(true);
											nodeFirstCriteria.configuration().sequenceTransitions().get(indexSecondCriteria).setTransitivelyExplainedTo(true);
											conflictFound = true;
											if(finalNode.configuration().sequenceTransitions().indexOf(tr) < indexFirstCriteria)
												statement = "In the model, after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() 
														+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" and \"" + psp.modelAutomaton().eventLabels().get(tr2.eventModel()) 
														+ "\" are concurrent, while in the log they are mutually exclusive";
											else
												statement = "In the model, after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(nodeFirstCriteria.configuration().sequenceTransitions().get(indexFirstCriteria).sourceNode()).stateModelID()).label() 
														+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" and \"" + psp.modelAutomaton().eventLabels().get(tr2.eventModel()) 
														+ "\" are concurrent, while in the log they are mutually exclusive";
										}
										else
										{
											//assert Log Conflict Model Sequential Mismatch
											tr.setExplainedTo(true);
											tr2.setTransitivelyExplainedTo(true);
											conflictFound = true;
											if(finalNode.configuration().sequenceTransitions().indexOf(tr) < commutativeConflictingFinalNode.configuration().sequenceTransitions().indexOf(tr2))
												statement = "In the model, after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr.sourceNode()).stateModelID()).label() 
														+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" occurs before task \"" + psp.modelAutomaton().eventLabels().get(tr2.eventModel()) 
														+ "\", while in the log they are mutually exclusive";
											else
												statement = "In the model, after marking \"" + psp.modelAutomaton().states().get(psp.nodes().get(tr2.sourceNode()).stateModelID()).label() 
														+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr2.eventModel()) + "\" occurs before task \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) 
														+ "\", while in the log they are mutually exclusive";
										}
									}
								}
							}
							if(!conflictFound && tr.sourceNode() == commutativeConflictingFinalNode.configuration().sequenceTransitions().get(indexFirstCriteria).sourceNode()
									&& !commutativeConflictingFinalNode.configuration().sequenceTransitions().get(indexFirstCriteria).isTransitivelyExplained()
									&& !commutativeConflictingFinalNode.configuration().sequenceTransitions().get(indexFirstCriteria).isExplained())
							{
								//assert Task Skipping Mismatch
								//i.e. Task was skipped in the Log, while sequential in the model
								tr.setExplainedTo(true);
								statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast()) 
										+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" is optional";
							}
						}
					}
				}
			}
		}
		if(!statement.isEmpty() && !this.statements().contains(statement))
			this.statements().add(statement);
	}
	
	public void discoverUnmatchedRepetition(Transition tr, Node finalNode, Multiset<Integer> finalConfiguration, IntArrayList potentialPath)
	{
		Transition testTr;
		int indexTestTr;
		String statement = "";
		if(tr.operation()==Configuration.Operation.LHIDE)
		{
			testTr = new Transition(Configuration.Operation.MATCH, tr.eventLog(), tr.eventLog());
			if((indexTestTr = finalNode.configuration().sequenceTransitions().indexOf(testTr)) != -1)
			{
				if(indexTestTr < finalNode.configuration().sequenceTransitions().indexOf(tr))
				{
					//assert Unmatched repetition mismatch
					tr.setExplainedTo(true);
					statement = "In the log, after \"" 
							+ psp.logAutomaton().eventLabels().get(psp.nodes().get(finalNode.configuration().sequenceTransitions().get(indexTestTr).sourceNode()).configuration().moveOnLog().getLast())
							+ "\", \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\" is repeated while in the model it is not";
				}
				else
				{
					//assert Unmatched repetition mismatch
					tr.setExplainedTo(true);
					statement = "In the log, after \"" 
							+ psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast())
							+ "\", \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\" is repeated while in the model it is not";
				}
			}
		}
		if(!statement.isEmpty() && !this.statements().contains(statement))
			this.statements().add(statement);
	}
	
	public void discoverTaskSubstitution(Transition tr, Node finalNode, Multiset<Integer> finalConfiguration, IntArrayList potentialPath)
	{
		Transition tr2;
		int indexTr;
		String statement = "";
		for(Node commutativeFinalNode : psp.concurrentCommutativePaths().get(finalConfiguration).get(potentialPath))
		{
			if(commutativeFinalNode.equals(finalNode)) continue;
			if((indexTr = finalNode.configuration().sequenceTransitions().indexOf(tr)) >= commutativeFinalNode.configuration().sequenceTransitions().size()) continue;
			tr2 = commutativeFinalNode.configuration().sequenceTransitions().get(indexTr);
			if(commutativeFinalNode.configuration().sequenceTransitions().contains(tr) 
			&& tr2.operation() != Configuration.Operation.MATCH && tr2.operation() != tr.operation() && !tr2.isExplained() && tr2.eventModel() != psp.modelAutomaton().skipEvent())
			{
				//assert Task Substitution
				tr.setExplainedTo(true);
				tr2.setTransitivelyExplainedTo(true);
				if(tr.operation() == Configuration.Operation.LHIDE)
				{
					if(tr.eventLog()==tr2.eventModel()) continue;
					statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().getLast()) 
					+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr2.eventModel()) + "\" is substituted by \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\"";
				}
				else
				{
					if(tr.eventModel()==tr2.eventLog()) continue;
					statement = "In the log, after \"" + psp.logAutomaton().eventLabels().get(psp.nodes().get(tr2.sourceNode()).configuration().moveOnLog().getLast()) 
							+ "\", \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\" is substituted by \"" + psp.logAutomaton().eventLabels().get(tr2.eventLog()) + "\"";
				}		
			}	
		}
		if(!statement.isEmpty() && !this.statements().contains(statement))
			this.statements().add(statement);
	}
	
	public void assertTaskAbsenceInsertion(Transition tr, Node finalNode, Multiset<Integer> finalConfiguration, IntArrayList potentialPath)
	{
		String statement;
		int indexTr;
		tr.setExplainedTo(true);
		if(tr.operation()==Configuration.Operation.LHIDE)
		{
			indexTr = psp.nodes().get(tr.sourceNode()).configuration().moveOnLog().size();
			statement = "In the log, \"" + psp.logAutomaton().eventLabels().get(tr.eventLog()) + "\"";
			if(indexTr==0)
				statement = statement + " occurs after the initial state";
			else
				statement = statement + " occurs after \"" + psp.logAutomaton().eventLabels().get(finalNode.configuration().moveOnLog().get(indexTr-1)) + "\"";
			if(indexTr == finalNode.configuration().moveOnLog().size()-1)
				statement = statement + " and before the final state";
			else
				statement = statement + " and before \"" + psp.logAutomaton().eventLabels().get(finalNode.configuration().moveOnLog().get(indexTr) + 1) + "\"";
		}	
		else
		{
			indexTr = psp.nodes().get(tr.sourceNode()).configuration().moveOnModel().size();
			//System.out.println(indexTr);
			statement = "In the model, \"" + psp.modelAutomaton().eventLabels().get(tr.eventModel()) + "\"";
			if(indexTr==0)
				statement = statement + " occurs after the start state";
			else
				statement = statement + " occurs after \"" + psp.modelAutomaton().eventLabels().get(finalNode.configuration().moveOnModel().get(indexTr-1)) + "\"";
			if(indexTr == finalNode.configuration().moveOnModel().size()-1)
				statement = statement + " and before the end state";
			else
				statement = statement + " and before \"" + psp.modelAutomaton().eventLabels().get(finalNode.configuration().moveOnModel().get(indexTr+1)) + "\"";
		}
		if(!this.statements().contains(statement))
			this.statements().add(statement);
	}
}