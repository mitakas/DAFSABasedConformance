package psp;

public class OldCode {
	@SuppressWarnings("unused")
	private void oldCodeFromConstructPSPinWhileNotToBeVisitedisEmpty()
	{
		//Code to fully enumerate the two Automatons according to Dumas Paper
	/*	
	for(Transition tlog : currentNode.log().outgoingTransitions())
	{
		if(!logOnly.contains(tlog) && !compEqual.containsKey(tlog))
			logOnly.add(tlog);
	}
	for(Transition tmodel : currentNode.model().outgoingTransitions())
	{
		if(!modelOnly.contains(tmodel) && !compEqual.containsValue(tmodel))
			modelOnly.add(tmodel);	
	}
}
else if((!currentNode.log().outgoingTransitions().isEmpty()) && currentNode.model().outgoingTransitions().isEmpty())
{
	for (Transition tlog : currentNode.log().outgoingTransitions())
	{
		logOnly.add(tlog);
	}
}
else if (currentNode.log().outgoingTransitions().isEmpty() && (!currentNode.model().outgoingTransitions().isEmpty()))
{
	for(Transition tmodel : currentNode.model().outgoingTransitions())
	{
		modelOnly.add(tmodel);
	}	
}
*/
/*
if(logOnly.size()>=0)
//if(logOnly.size()>=0 && modelOnly.size()==0)
{
	pw.println("#Possible Move on Log: " + logOnly.size());
	for(Transition tlog : logOnly)
	{
		potentialNode = new Node(tlog.target(),currentNode.model());
		if(!psp.nodes().values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
		{
			pw.println("Move on Log / Left hide: " + tlog.source().label() + " -> " +
					tlog.event().label() + " -> " + tlog.target().label());
			i++;
			toBeVisited.put(i, potentialNode);
			potentialArc = new Arc(1, tlog.event(), currentNode, potentialNode);
			psp.arcs().add(potentialArc);
			currentNode.outgoingArcs().add(potentialArc);
		}
			
	}
}
*//*
if(modelOnly.size()>=0)
//if(logOnly.size()==0 && modelOnly.size()>=0)
{
	pw.println("#Possible Move on Model: " + modelOnly.size());
	for(Transition tmodel : modelOnly)
	{
		potentialNode = new Node(currentNode.log(),tmodel.target());
		if(!psp.nodes().values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
		{
			pw.println("Move on Model / Right hide: " + tmodel.source().label() + " -> " +
					tmodel.event().label() + " -> " + tmodel.target().label());
			i++;
			toBeVisited.put(i, potentialNode);
			potentialArc = new Arc(2, tmodel.event(), currentNode, potentialNode);
			psp.arcs().add(potentialArc);
			currentNode.outgoingArcs().add(potentialArc);
		}
	}
}
*//*
int substitutions=0;
for(Transition tlog : logOnly)
{
	for(Transition tmodel : modelOnly)
	{
		potentialNode = new Node(tlog.target(),tmodel.target());
		if(!psp.nodes().values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
		{
			i++;
			toBeVisited.put(i, potentialNode);
			substitutions++;
		}
		
		if( extIn(tlog, tmodel.target().outgoingTransitions()))
		{
			System.out.println("Move on Model / Right hide: " + tmodel.source().label() + " -> " +
					tmodel.event().label() + " -> " + tmodel.target().label());
			potentialNode = new Node(currentNode.log(),tmodel.target());
			if(!visited.values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
			{
				i++;
				toBeVisited.put(i, potentialNode);
			}
		}
		
		if( extIn(tmodel, tlog.target().outgoingTransitions()))
		{
			System.out.println("Move on Log / Left hide: " + tlog.source().label() + " -> " +
					tlog.event().label() + " -> " + tlog.target().label());
			potentialNode = new Node(tlog.target(),currentNode.model());
			if(!visited.values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
			{
				i++;
				toBeVisited.put(i, potentialNode);
			}
		}
		
		boolean extInLog = false;
		boolean extInModel = false;
		for(Transition outmodel: currentNode.model().outgoingTransitions())
			extInLog =extInLog || extIn(tlog, outmodel.target().outgoingTransitions());
		for(Transition outlog : currentNode.log().outgoingTransitions())
			extInModel = extInModel || extIn(tmodel, outlog.target().outgoingTransitions());
		if(!(extInLog && extInModel))
		{
			System.out.println("Task Substitution from Log: " + tlog.source().label() +
					" -> " + tlog.event().label() + " -> " + tlog.target().label() +
					"| to Model: " + tmodel.source().label() + " -> "  +
					tmodel.event().label() + " -> " + tmodel.target().label());
			potentialNode = new Node(tlog.target(),tmodel.target());
			if(!visited.values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
			{
				i++;
				toBeVisited.put(i, potentialNode);
			}
		}
	}
	
}
pw.println("#Considered substitutions: " + substitutions);

pw.println("#Possible Synchronuous Moves: " + compEqual.size());
for(Transition tlog : compEqual.keySet())
{
	potentialNode = new Node(tlog.target(), compEqual.get(tlog).target());
	if(!psp.nodes().values().contains(potentialNode) && (!toBeVisited.values().contains(potentialNode)))
	{
		pw.println("Synchronuous Move on Log: " + tlog.source().label() +
				" -> " + tlog.event().label() + " -> " + tlog.target().label() +
				"| and on Model: " + compEqual.get(tlog).source().label() + " -> "  +
				compEqual.get(tlog).event().label() + " -> " + compEqual.get(tlog).target().label());
		i++;
		toBeVisited.put(i, potentialNode);
		potentialArc = new Arc(0, tlog.event(), currentNode, potentialNode);
		psp.arcs().add(potentialArc);
		currentNode.outgoingArcs().add(potentialArc);
	}
}
*/
	}
	@SuppressWarnings("unused")
	private void oldCodeFromConstructPSP_LookAheadFunction()
	{
		/*
		private static boolean extIn(Transition t, Set<Transition> transitions, visitedNodes)
		{
			if((!transitions.isEmpty()) && transitions.contains(t))
				return true;
			else if (transitions.isEmpty())
				return false;
			
			boolean extIn = false;
			for(Transition tr : transitions)
			{
				if(!tr.target().outgoingTransitions().isEmpty() && tr.target().outgoingTransitions().contains(t))
				{
					extIn = true;
					break;
				} 
				else if (!tr.target().outgoingTransitions().isEmpty())
				{
					//extIn = extIn || extIn(t,tr.target().outgoingTransitions());
					for(Transition transition : tr.target().outgoingTransitions())
					{
						extIn = extIn || (transition.target().outgoingTransitions().contains(t) &&
								(!transition.target().outgoingTransitions().isEmpty()));
						if(extIn)
							break;
					}
					
				}
				if(extIn)
					break;
				
			}
			
			return extIn;
		}*/
	}
}
