package psp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import com.google.common.collect.HashMultiset;
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

public class Configuration 
{
	private IntArrayList moveOnLog;
	private IntArrayList moveOnModel;
	private IntHashSet setMoveOnModel;
	private Multiset<Integer> setMoveOnLog;
	private List<Couple<Integer, Integer>> moveMatching;
	//private Node currentNode;
	public enum Operation {MATCH, LHIDE, RHIDE};
	private List<Transition> sequenceTransitions;
	private IntArrayList logIDs;
	private IntArrayList modelIDs;
	
	public Configuration(IntArrayList moveOnLog, IntArrayList moveOnModel, List<Couple<Integer, Integer>> moveMatching, 
			List<Transition> sequenceTransitions, IntArrayList logIDs, IntArrayList modelIDs)
	{
		this.moveOnLog = moveOnLog;
		this.setMoveOnLog = HashMultiset.create();
		for(int element : this.moveOnLog().distinct().toArray())
			setMoveOnLog.add(element, this.moveOnLog().count(t -> t==element));
		this.moveOnModel = moveOnModel;
		this.setMoveOnModel = new IntHashSet();
		this.setMoveOnModel().addAll(moveOnModel);
		this.moveMatching = moveMatching;
		//this.currentNode = currentNode;
		this.sequenceTransitions = sequenceTransitions;
		this.logIDs = logIDs;
		this.modelIDs = modelIDs;
	}
	
	public IntArrayList moveOnLog()
	{
		return this.moveOnLog;
	}
	
	public Multiset<Integer> setMoveOnLog()
	{
		return this.setMoveOnLog;
	}
	
	public IntArrayList moveOnModel()
	{
		return this.moveOnModel;
	}
	
	public IntHashSet setMoveOnModel()
	{
		return this.setMoveOnModel;
	}
	
	public void adjustSetMoveOnLog()
	{
		setMoveOnLog.clear();
		for(int element : this.moveOnLog().distinct().toArray())
			setMoveOnLog.add(element, this.moveOnLog().count(t -> t==element));
	}
	
	public void adjustSetMoveOnModel()
	{
		setMoveOnModel.clear();
		for(int element : this.moveOnModel().distinct().toArray())
			setMoveOnLog.add(element, this.moveOnModel().count(t -> t==element));
	}
	
	public List<Couple<Integer, Integer>> moveMatching()
	{
		return this.moveMatching;
	}
	/*
	public Node currentNode()
	{
		return this.currentNode;
	}
	*/
	public Configuration cloneConfiguration()
	{
		IntArrayList cloneMoveOnLog = new IntArrayList();
		cloneMoveOnLog.addAll(this.moveOnLog);
		IntArrayList cloneMoveOnModel = new IntArrayList();
		cloneMoveOnModel.addAll(this.moveOnModel());
		IntArrayList cloneLogIDs = new IntArrayList();
		cloneLogIDs.addAll(this.logIDs);
		IntArrayList cloneModelIDs = new IntArrayList();
		cloneModelIDs.addAll(this.modelIDs);
		return new Configuration(cloneMoveOnLog, cloneMoveOnModel, new ArrayList<Couple<Integer, Integer>>(this.moveMatching()), new ArrayList<Transition>(this.sequenceTransitions()), cloneLogIDs, cloneModelIDs);
	}
	
	public Configuration calculateSuffixFrom(Configuration configuration)
	{
		Configuration suffixConfiguration = this.cloneConfiguration();
		for(int i=1;i<=configuration.logIDs().size();i++) suffixConfiguration.logIDs().removeAtIndex(0);
		//suffixConfiguration.logIDs().removeAll(configuration.logIDs());
		for(int i=1;i<=configuration.modelIDs().size();i++) suffixConfiguration.modelIDs().removeAtIndex(0);
		//suffixConfiguration.modelIDs().removeAll(configuration.modelIDs());
		for(int i=1;i<=configuration.moveOnLog().size();i++) suffixConfiguration.moveOnLog().removeAtIndex(0);
		//suffixConfiguration.moveOnLog().removeAll(configuration.moveOnLog());
		for(int i=1;i<=configuration.moveOnModel().size();i++) suffixConfiguration.moveOnModel().removeAtIndex(0);
		//suffixConfiguration.moveOnModel().removeAll(configuration.moveOnModel());
		//for(int i=1;i<=configuration.moveMatching().size();i++) suffixConfiguration.moveMatching().remove(0);
		suffixConfiguration.moveMatching = suffixConfiguration.moveMatching().subList(configuration.moveMatching().size(), suffixConfiguration.moveMatching().size());
		//for(int i=1;i<=configuration.sequenceOperations().size();i++) suffixConfiguration.sequenceOperations().remove(0);
		suffixConfiguration.sequenceTransitions = suffixConfiguration.sequenceTransitions().subList(configuration.sequenceTransitions().size(), suffixConfiguration.sequenceTransitions().size());
		suffixConfiguration.adjustSetMoveOnLog();
		suffixConfiguration.adjustSetMoveOnModel();
		return suffixConfiguration;
	}
	
	public void addSuffixFrom(Configuration suffix)
	{
		this.logIDs().addAll(suffix.logIDs());
		this.modelIDs().addAll(suffix.modelIDs());
		this.moveOnLog().addAll(suffix.moveOnLog());
		this.moveOnModel().addAll(suffix.moveOnModel());
		this.moveMatching().addAll(suffix.moveMatching());
		this.sequenceTransitions().addAll(suffix.sequenceTransitions());
		this.adjustSetMoveOnLog();
		this.adjustSetMoveOnModel();
	}
	
	public List<Transition> sequenceTransitions()
	{
		return this.sequenceTransitions;
	}
	
	public IntArrayList logIDs()
	{
		return this.logIDs;
	}
	
	public IntArrayList modelIDs()
	{
		return this.modelIDs;
	}
	/*
	public String label()
	{
		String label = "Synchronuous Moves: {";
		for(Event e : this.moveMatching().keySet())
			label = label + "(" + e.label() + "; " + this.moveMatching().get(e).label() + "), ";
		label = label.substring(0, label.length() - 2) + "}\n";
		label = label + "Move on Log: {";
		for(Event e : this.moveOnLog())
			label = label + e.label() + ", ";
		label = label.substring(0, label.length() - 2) + "}\n";
		label = label + "Move on Model: {";
		for(Event e : this.moveOnModel())
			label = label + e.label() + ", ";
		label = label.substring(0, label.length() - 2) + "}";
		return label;
	}
	*/
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration configuration = (Configuration) o;

        return new EqualsBuilder()
        		.append(this.moveMatching(), configuration.moveMatching())
        		.append(this.moveOnLog(), configuration.moveOnLog())
        		.append(this.moveOnModel(), configuration.moveOnModel())
        		//.append(this.currentNode(), configuration.currentNode())
        		.append(this.sequenceTransitions(), configuration.sequenceTransitions())
        		.append(this.logIDs(), configuration.logIDs())
        		.append(this.modelIDs(), configuration.modelIDs())
        		.isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,37)
        		.append(this.moveMatching())
        		.append(this.moveOnLog())
        		.append(this.moveOnModel())
        		//.append(this.currentNode())
        		.append(this.sequenceTransitions())
        		.append(this.logIDs())
        		.append(this.modelIDs())
        		.toHashCode();
    }
}
