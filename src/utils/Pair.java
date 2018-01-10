package utils;

/**
 * <p>пара значений</p>
 * 
 * @author falcon-it
 *
 * @param <T1>
 * @param <T2>
 */
public final class Pair<T1, T2> {
	protected T1 m_First;
	protected T2 m_Second;
	
	public Pair() {
		m_First = null;
		m_Second = null;
	}
	public Pair(T1 first, T2 second) {
		m_First = first;
		m_Second = second;
	}
	
	public final T1 getFirst() { return m_First; }
	public final void putFirst(T1 first) { m_First = first; }
	
	public final T2 getSecond() { return m_Second; }
	public final void putSecond(T2 second) { m_Second = second; }
}
