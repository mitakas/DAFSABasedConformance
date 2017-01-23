package psp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multiset;

import Automaton.Automaton;

public class PSP 
{
	private BiMap<Integer, Node> nodes;
	private Set<Arc> arcs;
	//private Set<Configuration> configurations;
	private int sourceNode;
	private IntHashSet finalNodes;
	private BiMap<Multiset<Integer>, BiMap<IntArrayList, Set<Node>>> concurrentCommutativePaths;
	private Automaton logAutomaton;
	private Automaton modelAutomaton;
	
	public PSP(BiMap<Integer, Node> nodes, Set<Arc> arcs, int sourceNode, Automaton logAutomaton, Automaton modelAutomaton)
	{
		this.nodes = nodes;
		this.arcs = arcs;
		this.sourceNode = sourceNode;
		this.finalNodes = new IntHashSet();
		this.logAutomaton = logAutomaton;
		this.modelAutomaton = modelAutomaton;
	}
	
	public BiMap<Integer, Node> nodes()
	{
		return this.nodes;
	}
	
	public Set<Arc> arcs()
	{
		return this.arcs;
	}
	
	public Node sourceNode()
	{
		return this.nodes().get(this.sourceNode);
	}
	
	public IntHashSet finalNodes()
	{
		return this.finalNodes;
	}
	
	public Automaton logAutomaton()
	{
		return this.logAutomaton;
	}
	
	public Automaton modelAutomaton()
	{
		return this.modelAutomaton;
	}
	
	public BiMap<Multiset<Integer>, BiMap<IntArrayList,Set<Node>>> concurrentCommutativePaths()
	{
		if(this.concurrentCommutativePaths==null)
			this.concurrentCommutativePaths = HashBiMap.create();
		return this.concurrentCommutativePaths;
	}
	
	public String arcLabel(Arc arc)
	{
		if(arc.transition().operation() == Configuration.Operation.RHIDE)
			return "< " + arc.transition().operation() + " : " + this.modelAutomaton().eventLabels().get(arc.transition().eventModel()) + " >";
		return "< " + arc.transition().operation() + " : " + this.logAutomaton().eventLabels().get(arc.transition().eventLog()) + " >";
	}
	
	public String nodeLabel(int nodeID)
	{
		String nodeLabel = "Matches = { ";
		for(Couple<Integer, Integer> events : this.nodes().get(nodeID).configuration().moveMatching())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(events.getFirstElement()) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Log = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Log = { ";
		for(int event : this.nodes().get(nodeID).configuration().moveOnLog().toArray())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Model = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Model = { ";
		for(int event : this.nodes().get(nodeID).configuration().moveOnModel().toArray())
			nodeLabel = nodeLabel + this.modelAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }";
		nodeLabel = nodeLabel + "<br/>Node weight: " + this.nodes().get(nodeID).weight();
		return nodeLabel;
	}
	
	public String nodeLabel(Node node)
	{
		String nodeLabel = "Matches = { ";
		for(Couple<Integer, Integer> events : node.configuration().moveMatching())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(events.getFirstElement()) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Log = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Log = { ";
		for(int event : node.configuration().moveOnLog().toArray())
			nodeLabel = nodeLabel + this.logAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }<br/>Moves on Model = { ";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }<br/>Moves on Model = { ";
		for(int event : node.configuration().moveOnModel().toArray())
			nodeLabel = nodeLabel + this.modelAutomaton().eventLabels().get(event) + "; ";
		if(nodeLabel.substring(nodeLabel.length()-2, nodeLabel.length()).equals("{ "))
			nodeLabel = nodeLabel + "\u2205 }";
		else
			nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 2) + " }";
		//nodeLabel = nodeLabel + "<br/>Node weight: " + node.weight();
		return nodeLabel;
	}
	
	public void toDot(PrintWriter pw) throws IOException {
		pw.println("digraph fsm {");
		pw.println("rankdir=LR;");
		pw.println("node [shape=box,style=filled, fillcolor=white]");
		
		for(Node node : this.nodes().values()) {
			if(node.equals(this.sourceNode())) {
				pw.printf("%d [label=<%s>, fillcolor=\"gray\"];%n", node.hashCode(), this.nodeLabel(node));
			} else {
				pw.printf("%d [label=<%s>];%n", node.hashCode(), this.nodeLabel(node));
			}
			
			for(Arc arc : node.outgoingArcs()) {
				pw.printf("%d -> %d [label=\"%s\"];%n", node.hashCode(), arc.target().hashCode(), this.arcLabel(arc));
			}

			if(node.isFinal()) {
				pw.printf("%d [label=<%s>, style=\"bold\"];%n", node.hashCode(), this.nodeLabel(node));
			}
		}
		pw.println("}");
	}
	
	public void toDot(String fileName) throws IOException {
		PrintWriter pw = new PrintWriter(fileName);
		toDot(pw);
		pw.close();
	}
}
