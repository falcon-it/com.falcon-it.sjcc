package packet;

/**
 * интерфейс читателя базовых типов из объекта ввода/вывода
 * @author Ilya Sokolov
 * @param <ReadObjectType> тип объекта ввода/вывода
 */
public interface Reader<ReadObjectType> {
	/**
	 * @param in
	 * @param b массив байт в который будут записанны прочитанные данные длинной b.length
	 * @throws PacketIOException
	 */
	void readBytes(ReadObjectType in, byte[] b) throws PacketIOException;
	/**
	 * @param in
	 * @param b массив байт в который будут записанны прочитанные данные
	 * @param off смещение от начала массива
	 * @param len количество читаемых байт
	 * @throws PacketIOException
	 */
	void readBytes(ReadObjectType in, byte[] b, int off, int len) throws PacketIOException;
	/**
	 * @param in
	 * @return boolean
	 * @throws PacketIOException
	 */
	boolean readBoolean(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return byte
	 * @throws PacketIOException
	 */
	byte readByte(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return char
	 * @throws PacketIOException
	 */
	char readChar(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return double
	 * @throws PacketIOException
	 */
	double readDouble(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return float
	 * @throws PacketIOException
	 */
	float readFloat(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return int
	 * @throws PacketIOException
	 */
	int readInt(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return long
	 * @throws PacketIOException
	 */
	long readLong(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return short
	 * @throws PacketIOException
	 */
	short readShort(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return String
	 * @throws PacketIOException
	 */
	String readString(ReadObjectType in) throws PacketIOException;
	/**
	 * @param in
	 * @return Object
	 * @throws PacketIOException
	 */
	<ObjectType> ObjectType readObject(ReadObjectType in) throws PacketIOException;
}
