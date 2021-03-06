package packet.serialize;

import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Writer;

/**
 * boolean
 * @author Ilya Sokolov
 */
public final class BooleanSerialize extends BaseSerialize {
	public static final Class<?>[] classes = new Class<?>[] { boolean.class, Boolean.class };
	public static final int[] classesIDs = new int[] { Registry.calculateThisClassID(classes[0]), Registry.calculateThisClassID(classes[1]) };
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClasses()
	 */
	@Override
	public Class<?>[] supportedClasses() { return classes; }
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClassesIDs(int)
	 */
	@Override
	public int[] supportedClassesIDs() { return classesIDs; }
	/* (non-Javadoc)
	 * @see packet.TypeSerializer#read(java.lang.Object, packet.Registry, packet.Reader)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader) throws PacketIOException {
		return (T)(Boolean)reader.readBoolean(in);
	}
	/* (non-Javadoc)
	 * @see packet.TypeSerializer#write(java.lang.Object, java.lang.Object, packet.Registry, packet.Writer)
	 */
	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer) throws PacketIOException {
		writer.writeBoolean(out, (Boolean)v);
	}
}
