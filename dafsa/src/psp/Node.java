package psp;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
