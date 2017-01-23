package psp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
