package mdt.cli.list.push;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ListCollector {
	public void collectLine(Object[] cols);
	public String getFinalString();
}