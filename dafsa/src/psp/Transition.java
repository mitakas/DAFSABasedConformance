package psp;

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

public class Transition {
	
	private Configuration.Operation operation;
	private int eventLog = -1;
	private int eventModel = -1;
	private int targetLog;
	private int targetModel;
	private boolean explained = false;
	private boolean transitivelyExplained = false;
	private int sourceNode = -1;
	
	public Transition(Configuration.Operation operation, int eventLog, int eventModel)
	{
		this.operation = operation;
		if(operation==Configuration.Operation.MATCH)
		{
			this.eventLog = eventLog;
			this.eventModel = eventModel;
		}
		else if(operation==Configuration.Operation.LHIDE)
			this.eventLog = eventLog;
		else
			this.eventModel = eventModel;
	}
	
	public Transition(Configuration.Operation operation, int eventLog, int eventModel, int targetLog, int targetModel, int sourceNode)
	{
		this.operation = operation;
		this.sourceNode = sourceNode;
		
		if(operation==Configuration.Operation.MATCH)
		{
			this.eventLog = eventLog;
			this.eventModel = eventModel;
			this.targetLog = targetLog;
			this.targetModel = targetModel;
		}
		else if(operation==Configuration.Operation.LHIDE)
		{
			this.eventLog = eventLog;
			this.targetLog = targetLog;
		}
		else
		{
			this.eventModel = eventModel;
			this.targetModel = targetModel;
		}
	}
	
	public Configuration.Operation operation()
	{
		return this.operation;
	}
	
	public int eventLog()
	{
		if(this.operation!=Configuration.Operation.RHIDE)
			return this.eventLog;
		return -1;
	}
	
	public int eventModel()
	{
		if(this.operation!=Configuration.Operation.LHIDE)
			return this.eventModel;
		return -1;
	}
	
	public int targetLog()
	{
		if(this.operation!=Configuration.Operation.RHIDE)
			return this.targetLog;
		return -1;
	}
	
	public int targetModel()
	{
		if(this.operation!=Configuration.Operation.LHIDE)
			return this.targetModel;
		return -1;
	}
	
	public boolean isExplained()
	{
		return this.explained;
	}
	
	public void setExplainedTo(boolean explained)
	{
		this.explained = explained;
	}
	
	public boolean isTransitivelyExplained()
	{
		return this.transitivelyExplained;
	}
	
	public void setTransitivelyExplainedTo(boolean transitivelyExplained)
	{
		this.transitivelyExplained = transitivelyExplained;
	}
	
	public int sourceNode()
	{
		return this.sourceNode;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition transition = (Transition) o;
        
        if(this.operation()==Configuration.Operation.MATCH)
        	return new EqualsBuilder()
        		.append(this.operation(), transition.operation())
        		.append(this.eventLog(), transition.eventLog())
        		.append(this.eventModel(), transition.eventModel())
        		.isEquals();
        else if(this.operation()==Configuration.Operation.LHIDE)
        	return new EqualsBuilder()
            		.append(this.operation(), transition.operation())
            		.append(this.eventLog(), transition.eventLog())
            		.isEquals();
        else
        	return new EqualsBuilder()
            		.append(this.operation(), transition.operation())
            		.append(this.eventModel(), transition.eventModel())
            		.isEquals();
    }

    @Override
    public int hashCode() {
    	if(this.operation()==Configuration.Operation.MATCH)
        	return new HashCodeBuilder(17,37)
        		.append(this.operation())
        		.append(this.eventLog())
        		.append(this.eventModel())
        		.toHashCode();
        else if(this.operation()==Configuration.Operation.LHIDE)
        	return new HashCodeBuilder(17,37)
            		.append(this.operation())
            		.append(this.eventLog())
            		.toHashCode();
        else
        	return new HashCodeBuilder(17,37)
            		.append(this.operation())
            		.append(this.eventModel())
            		.toHashCode();
    }
}
