package packet;

/**
 * интерфейс писатаеля базовых типов из объекта ввода/вывода
 * @author Ilya Sokolov
 * @param <WriteObjectType> тип объекта ввода/вывода
 */
public interface Writer<WriteObjectType> {
	/**
	 * @param out
	 * @param b массив байт из которого будут записаны данные длинной b.length
	 * @throws PacketIOException
	 */
	void write(WriteObjectType out, byte[] b) throws PacketIOException;
	/**
	 * @param out
	 * @param b массив байт будут записаны данные
	 * @param off смещение от начала массива
	 * @param len количество записываемых байт
	 * @throws PacketIOException
	 */
	void write(WriteObjectType out, byte[] b, int off, int len) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeBoolean(WriteObjectType out, boolean v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeByte(WriteObjectType out, byte v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeChar(WriteObjectType out, char v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeDouble(WriteObjectType out, double v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeFloat(WriteObjectType out, float v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeInt(WriteObjectType out, int v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeLong(WriteObjectType out, long v) throws PacketIOException;
	/**
	 * @param out
	 * @param v
	 * @throws PacketIOException
	 */
	void writeShort(WriteObjectType out, short v) throws PacketIOException;
	/**
	 * @param out
	 * @param s
	 * @throws PacketIOException
	 */
	void writeString(WriteObjectType out, String v) throws PacketIOException;
	/**
	 * @param out
	 * @param obj
	 * @throws PacketIOException
	 */
	void writeObject(WriteObjectType out, Object v) throws PacketIOException;
}
