package packet.serialize;

import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Writer;

/**
 * Byte
 * @author Ilya Sokolov
 */
public final class ByteSerialize extends BaseSerialize {
	public static final Class<?>[] classes = new Class<?>[] {byte.class, Byte.class};
	public static final int[] classesID = new int[] {Registry.calculateClassID(classes[0]), Registry.calculateClassID(classes[1])};
	/* (non-Javadoc)
	 * @see packet.Serialize#classes()
	 */
	@Override
	public Class<?>[] classes() { return classes; }
	/* (non-Javadoc)
	 * @see packet.Serialize#ids(int)
	 */
	@Override
	public int[] ids() { return classesID; }
	/* (non-Javadoc)
	 * @see packet.Serialize#read(java.lang.Object, packet.Registry, packet.Reader)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		return (T)(Byte)reader.readByte(in);
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#write(java.lang.Object, java.lang.Object, packet.Registry, packet.Writer)
	 */
	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		writer.writeByte(out, (Byte)v);
	}
}
