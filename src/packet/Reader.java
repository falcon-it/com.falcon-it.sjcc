package packet;

/**
 * интерфейс читателя базовых типов из объекта ввода/вывода
 * @author Ilya Sokolov
 * @param <ReadObjectType> тип объекта ввода/вывода
 */
public interface Reader<ReadObjectType> {
	/**
	 * @param reader
	 * @return boolean
	 */
	boolean readBoolean(ReadObjectType reader);
	/**
	 * @param reader
	 * @return byte
	 */
	byte readByte(ReadObjectType reader);
	/**
	 * @param reader
	 * @return char
	 */
	char readChar(ReadObjectType reader);
	/**
	 * @param reader
	 * @return double
	 */
	double readDouble(ReadObjectType reader);
	/**
	 * @param reader
	 * @return float
	 */
	float readFloat(ReadObjectType reader);
	/**
	 * @param reader
	 * @param b массив байт в который будут записанны прочитанные данные длинной b.length
	 */
	void readBytes(ReadObjectType reader, byte[] b);
	/**
	 * @param reader
	 * @param b массив байт в который будут записанны прочитанные данные
	 * @param off смещение от начала массива
	 * @param len количество читаемых байт
	 */
	void readBytes(ReadObjectType reader, byte[] b, int off, int len);
	/**
	 * @param reader
	 * @return int
	 */
	int readInt(ReadObjectType reader);
	/**
	 * @param reader
	 * @return long
	 */
	long readLong(ReadObjectType reader);
	/**
	 * @param reader
	 * @return short
	 */
	short readShort(ReadObjectType reader);
	/**
	 * @param reader
	 * @return unsigned byte
	 */
	int readUnsignedByte(ReadObjectType reader);
	/**
	 * @param reader
	 * @return unsigned short
	 */
	int readUnsignedShort(ReadObjectType reader);
	/**
	 * @param reader
	 * @return String
	 */
	String readUTF(ReadObjectType reader);
	/**
	 * @param reader
	 * @return Object
	 */
	<ObjectType> ObjectType readObject(ReadObjectType reader);
}
