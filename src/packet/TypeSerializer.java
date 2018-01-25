package packet;

/**
 * @author Ilya Sokolov
 */
public interface TypeSerializer {
	<T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader) throws PacketIOException;
	<T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer) throws PacketIOException;
}
