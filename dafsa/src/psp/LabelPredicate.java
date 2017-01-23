package psp;

import java.util.function.Predicate;

public class LabelPredicate<t> implements Predicate<t> {
	t varc1;
	
	public LabelPredicate(t t)
	{
		this.varc1 = t;
	}
	
	@Override
	public boolean test(t t) 
	{
		if(varc1.equals(t))
			return true;
		return false;
	}
}

