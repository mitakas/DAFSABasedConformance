package name.kazennikov.dafsa;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

import name.kazennikov.fsa.IntFSAObjectEventHandler;

import com.google.common.base.Objects;

/**
 * DAFSA with integer labels and set of integers as final feature
 * @author Anton Kazennikov
 *
 */
public class IntDAFSAInt extends AbstractIntDAFSA {
	
	ArrayList<TIntHashSet> finals;
	
	public IntDAFSAInt() {
		super();
	}

	@Override
	public int finalHash(int state) {
		return finals.get(state).hashCode();
	}

	@Override
	public boolean finalEquals(int state1, int state2) {
		return Objects.equal(finals.get(state1), finals.get(state2));
	}

	@Override
	public void finalReset(int state) {
		finals.get(state).clear();
		states.get(state).validHashCode = false;
		
	}

	@Override
	public void finalAssign(int destState, int srcState) {
		TIntHashSet dest = finals.get(destState);
		TIntHashSet src = finals.get(srcState);
		dest.clear();
		dest.addAll(src);

	}
	
	@Override
	public void initFinals() {
		finals = new ArrayList<>();
	}

	@Override
	public void newFinal(int state) {
		finals.add(new TIntHashSet(3));
		
	}
	
	int finalValue;
	
	public void setFinalValue(int finalValue) {
		this.finalValue = finalValue;
	}

	@Override
	public boolean setFinal(int state) {
		boolean b = finals.get(state).add(finalValue);
		states.get(state).validHashCode = false;
		return b;
	}

	@Override
	public boolean hasFinal(int state) {
		return finals.get(state).contains(finalValue);
	}
	
	public TIntHashSet getFinals(int state) {
		return finals.get(state);
	}

	@Override
	public boolean isFinalState(int state) {
		return !finals.get(state).isEmpty();
	}
	
	/**
	 * Emit current FSA state to events 
	 * @param events events object
	 */
	public void emit(IntFSAObjectEventHandler<int[]> events) {
		for(int i = 0; i < states.size(); i++) {
			State s = states.get(i);
			events.startState(i);
			
			events.setFinalValue(finals.get(i).toArray());
			events.setFinal();
			
			for(int j = 0; j < s.next.size(); j++) {
				int input = decodeLabel(s.next.get(j));
				int dest = decodeDest(s.next.get(j));
				events.addTransition(input, dest);
			}
			
			events.endState();
		}
	}


}