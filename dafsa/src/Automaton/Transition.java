package Automaton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
