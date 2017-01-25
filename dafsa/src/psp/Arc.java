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

public class Arc 
{
	//Operations as Enumerations?;
	private Transition transition;
	private Node source;
	private Node target;
	
	public Arc(Transition transition, Node source, Node target)
	{
		this.transition = transition;
		this.source = source;
		this.target = target;
	}
	
	public Transition transition()
	{	
		return this.transition;
	}
	
	public Node source()
	{
		return this.source;
	}
	public Node target()
	{
		return this.target;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Arc arc = (Arc) o;

        return new EqualsBuilder()
        		.append(this.transition(), arc.transition())
        		.append(this.source(), arc.source())
        		.append(this.target(), arc.target())
        		.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
        		.append(this.transition())
        		.append(this.source())
        		.append(this.target())
        		.toHashCode();
    }
}
