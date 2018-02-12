package packet;

/**
 * интерфейс сериалайзера
 * @author Ilya Sokolov
 */
public interface Serialize {
	/**
	 * возникает при попытке добавать в реест тип с таким же id
	 */
	@SuppressWarnings("serial")
	public static final class NotFoundTypeIDException extends PacketException {
		public NotFoundTypeIDException() { super(); }
	}
	/**
	 * @return массив классов, поддерживаемых сериалайзером
	 */
	Class<?>[] supportedClasses();
	/**
	 * @return массив id типов
	 */
	int[] supportedClassesIDs();
	/**
	 * @param tid id типа
	 * @return класс по id типа
	 */
	<T> Class<T> classByID(int tid) throws NotFoundTypeIDException;
	/**
	 * @param tid id типа
	 * @return экземпляр по id типа
	 */
	<T> T newInstance(int tid) throws NotFoundTypeIDException, InstantiationException, IllegalAccessException;
	/**
	 * метод для чтения
	 * @param in объект для чтения
	 * @param reg реестр типов
	 * @param reader читатель базовых типов
	 * @return вернуть почитанный экземпляр
	 * @throws PacketIOException
	 */
	<T, ReadObjectType> T read(
			ReadObjectType in, 
			Registry reg, 
			Reader<ReadObjectType> reader) throws PacketIOException;
	/**
	 * метод для записи
	 * @param out объет для записи
	 * @param v записываемый экземпляр
	 * @param reg реестр типов
	 * @param writer писатель базовых типов
	 * @throws PacketIOException
	 */
	<T, WriteObjectType> void write(
			WriteObjectType out, 
			T v, 
			Registry reg, 
			Writer<WriteObjectType> writer) throws PacketIOException;
}
