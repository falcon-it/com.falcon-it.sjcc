package packet.serialize;

import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Serialize;
import packet.Writer;

/**
 * boolean
 * @author Ilya Sokolov
 */
public final class BooleanSerialize implements Serialize {
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
