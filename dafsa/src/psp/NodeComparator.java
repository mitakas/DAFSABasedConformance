package psp;
import java.util.Comparator;

public class NodeComparator implements Comparator<Node>
{
	@Override
	public int compare(Node x, Node y)
	{
		return x.weight() - y.weight();
	}
}
