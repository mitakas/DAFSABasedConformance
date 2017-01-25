package psp;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

public class Node {

	private int stateLogID;
	private int stateModelID;
	private Configuration configuration;
	private Set<Arc> outgoingArcs;
	private int weight;
	private boolean isFinal;
	private boolean discover = true;
	
	public Node(int stateLogID, int stateModelID, Configuration configuration, int weight)
	{
		this.stateLogID = stateLogID;
		this.stateModelID = stateModelID;
		this.configuration = configuration;
		this.weight = weight;
		this.isFinal = false;
	}
	
	public Node(int stateLogID, int stateModelID, Configuration configuration, int weight, boolean discover)
	{
		this.stateLogID = stateLogID;
		this.stateModelID = stateModelID;
		this.configuration = configuration;
		this.weight = weight;
		this.isFinal = false;
		this.discover = discover;
	}
	
	public int stateLogID()
	{
		return this.stateLogID;
	}
	
	public int stateModelID()
	{
		return this.stateModelID;
	}
	
	public Configuration configuration()
	{
		return this.configuration;
	}
	
	public Set<Arc> outgoingArcs()
	{
		if(this.outgoingArcs==null)
			this.outgoingArcs = new HashSet<Arc>();
		return this.outgoingArcs;
	}
	
	public int weight()
	{
		return this.weight;
	}
	
	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public void isFinal(boolean isFinal)
	{
		this.isFinal = isFinal;
	}
	
	public boolean discover()
	{
		return this.discover;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return new EqualsBuilder()
        		.append(this.stateLogID(), node.stateLogID())
        		.append(this.stateModelID(), node.stateModelID())
        		.append(this.configuration(), node.configuration())
        		//.append(this.weight(), node.weight())
        		//.append(this.isFinal(), node.isFinal())
        		.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
        		.append(this.stateLogID())
        		.append(this.stateModelID())
        		.append(this.configuration())
        		//.append(this.weight())
        		//.append(this.isFinal())
        		.toHashCode();
    }
}
