package Automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.*;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import com.google.common.collect.Multiset;

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

public class State {
	private int id;
	private String label;
	private Set<Transition> outgoingTransitions;
	private Set<Transition> incomingTransitions;
	private boolean isSource;
	private boolean isFinal;
	private int component = -1;
	private Map<Integer, Set<Multiset<Integer>>> possibleFutures;
	private Map<Multiset<Integer>, Map<IntArrayList, IntArrayList>> potentialPathsAndTraceLabels;
	//private	Map<IntArrayList, IntArrayList> traceLabels;
	
	protected State(){}
	public State(int id, boolean isSource, boolean isFinal)
	{
		if (!(id>=0)) {return;}
		this.id = id;
		this.label = "" + id;
		this.isSource = isSource;
		this.isFinal = isFinal;
		
	}
	
	public State(int id, String label, boolean isSource, boolean isFinal)
	{
		if (!(id>=0)) {return;}
		this.id = id;
		this.label = label;
		if(this.label.contains("<html>") && this.label.contains("</html>"))
			this.label = this.label.substring(6, this.label.length()-7);
		this.isSource = isSource;
		this.isFinal = isFinal;
		
	}
	
	/*
	protected void setOutgoingTransitions(Set<Transition> allTransitions)
	{
		
		for (Transition t : allTransitions)
		{
			if (t.source().id()==this.id()) this.outgoingTransitions.add(t);
		}
	}
	*/
	public int id()
	{
		return this.id;
	}
	
	public String label()
	{
		return this.label;
	}
	
	public Set<Transition> outgoingTransitions()
	{
		if (this.outgoingTransitions == null){this.outgoingTransitions = new HashSet<Transition>();}
		return this.outgoingTransitions;
	}
	
	public Set<Transition> incomingTransitions()
	{
		if (this.incomingTransitions == null){this.incomingTransitions = new HashSet<Transition>();}
		return this.incomingTransitions;
	}
	
	public boolean isSource()
	{
		return this.isSource;
	}
	
	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public Map<Integer, Set<Multiset<Integer>>> possibleFutures()
	{
		if(this.possibleFutures == null)
			this.possibleFutures = new HashMap<Integer, Set<Multiset<Integer>>>();
		return this.possibleFutures;
	}
	
	public Map<Multiset<Integer>, Map<IntArrayList, IntArrayList>> potentialPathsAndTraceLabels()
	{
		if(!this.isFinal())
			return null;
		if(this.potentialPathsAndTraceLabels == null)
			this.potentialPathsAndTraceLabels = new HashMap<Multiset<Integer>, Map<IntArrayList, IntArrayList>>();
		return this.potentialPathsAndTraceLabels;
	}
	
	public void setComponent(int component)
	{
		this.component = component;
	}
	
	public int component()
	{
		return this.component;
	}
	
//	public Map<IntArrayList, IntArrayList> traceLabels()
//	{
//		if(!this.isFinal())	return null;
//		if(this.traceLabels==null)
//			this.traceLabels = new HashMap<IntArrayList, IntArrayList>();
//		return this.traceLabels;
//	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        return new EqualsBuilder()
        		.append(this.id(), state.id())
        		.append(this.label(), state.label())
        		.append(this.outgoingTransitions(), state.outgoingTransitions())
        		.append(this.isSource(), state.isSource())
        		.append(this.isFinal(), state.isFinal())
        		.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
        		.append(this.id())
        		.append(this.label())
        		.append(this.isSource())
        		.append(this.isFinal())
        		.toHashCode();
    }
}
