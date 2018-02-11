package packet.serialize;

import packet.Serialize;

/**
 * базовый сериалайзер
 * реализует дефолтные операции поиска классов и создания экземпляров
 * @author Ilya Sokolov
 */
public abstract class BaseSerialize implements Serialize {
	/* (non-Javadoc)
	 * @see packet.Serialize#classByID(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<T> classByID(int tid) throws NotFoundTypeIDException {
		int[] arrID = ids();
		for(int i = 0; i < arrID.length; ++i) {
			if(arrID[i] == tid) { return (Class<T>) classes()[i]; }
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#newInstance(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(int tid) 
			throws NotFoundTypeIDException, InstantiationException, IllegalAccessException {
		return (T) classByID(tid).newInstance();
	}
}
