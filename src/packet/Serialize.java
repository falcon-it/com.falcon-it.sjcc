package packet;

/**
 * интерфейс сериалайзера
 * @author Ilya Sokolov
 */
public interface Serialize {
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
