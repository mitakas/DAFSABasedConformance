package Automaton;

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
	
	private State source;
	private State target;
	private int eventID;
	
	public Transition(State source, State target, int eventID)
	{
		this.source = source;
		this.target = target;
		this.eventID = eventID;
	}

	public State source()
	{
		return this.source;
	}
	
	public State target()
	{
		return this.target;
	}
	
	public int eventID()
	{
		return this.eventID;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition transition = (Transition) o;

        return new EqualsBuilder()
        		.append(this.source(), transition.source())
        		.append(this.target(), transition.target())
        		.append(this.eventID(), transition.eventID())
        		.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
        		.append(this.source())
        		.append(this.target())
        		.append(this.eventID())
        		.toHashCode();
    }
}
