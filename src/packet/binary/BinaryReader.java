package packet.binary;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import packet.InputStreamDispatcher;
import packet.PacketIOException;
import packet.Reader;

/**
 * реализация интерфейса читателя для бинароного вывода
 * @author Ilya Sokolow
 * @param <ReadObjectType>
 */
public final class BinaryReader<ReadObjectType extends InputStream> implements Reader<ReadObjectType> {
	private final Object m_SynchObject = new Object();
	private DataInputStream m_DataInputStream = null;
	private InputStreamDispatcher m_DataInputStreamDispatcher = null;
	private ObjectInputStream m_ObjectInputStream = null;
	private InputStreamDispatcher m_ObjectInputStreamDispatcher = null;
	
	/*
	 * read Data
	 */
	
	private interface readData <T> {
		T read(DataInputStream data_in) throws IOException;
	}
	
	private final <T> T readData(readData<T> read, InputStream in) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataInputStream == null) {
				m_DataInputStreamDispatcher = new InputStreamDispatcher();
				m_DataInputStream = new DataInputStream(m_DataInputStreamDispatcher);
			}
		}
		
		synchronized (m_DataInputStreamDispatcher) {
			m_DataInputStreamDispatcher.putStream(in);
			try {
				return read.read(m_DataInputStream);
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataInputStreamDispatcher.putStream(null);
			}
		}
	}
	
	@Override
	public boolean readBoolean(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readBoolean(); }, in);
	}
}
