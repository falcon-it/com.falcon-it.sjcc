package packet.serialize;

import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Serialize;
import packet.Writer;

/**
 * array
 * @author Ilya Sokolov
 */
public final class ArraySerialize implements Serialize {

	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		// TODO Auto-generated method stub

	}

}
