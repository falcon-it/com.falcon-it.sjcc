package utils;

/**
 * три значения
 * @author ilya
 * 
 * @param <L>
 * @param <M>
 * @param <R>
 */
public final class Triple<L, M, R> {
	private L m_Left;
	private M m_Middle;
	private R m_Right;
	
	public Triple() {
		m_Left = null;
		m_Middle = null;
		m_Right = null;
	}
	public Triple(L l, M m, R r) {
		m_Left = l;
		m_Middle = m;
		m_Right = r;
	}
	
	public final L getLeft() { return m_Left; }
	public final void putLeft(L l) { m_Left = l; }

	public final M getMiddle() { return m_Middle; }
	public final void putMiddle(M m) { m_Middle = m; }

	public final R getRight() { return m_Right; }
	public final void putRight(R r) { m_Right = r; }
}
