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
	 * read byte array
	 */
	
	private final void readByteArray(InputStream in, byte[] b, int off, int len) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataInputStream == null) {
				m_DataInputStreamDispatcher = new InputStreamDispatcher();
				m_DataInputStream = new DataInputStream(m_DataInputStreamDispatcher);
			}
		}
		
		synchronized (m_DataInputStreamDispatcher) {
			m_DataInputStreamDispatcher.putStream(in);
			try {
				m_DataInputStream.read(b, off, len);
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataInputStreamDispatcher.putStream(null);
			}
		}
	}
	
	@Override
	public void readBytes(ReadObjectType in, byte[] b) throws PacketIOException {
		readByteArray(in, b, 0, b.length);
	}
	
	@Override
	public void readBytes(ReadObjectType in, byte[] b, int off, int len) throws PacketIOException {
		readByteArray(in, b, off, len);
	}
	
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
	
	@Override
	public byte readByte(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readByte(); }, in);
	}
	
	@Override
	public char readChar(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readChar(); }, in);
	}
	
	@Override
	public double readDouble(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readDouble(); }, in);
	}
	
	@Override
	public float readFloat(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readFloat(); }, in);
	}
	
	@Override
	public int readInt(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readInt(); }, in);
	}
	
	@Override
	public long readLong(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readLong(); }, in);
	}
	
	@Override
	public short readShort(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readShort(); }, in);
	}
	
	@Override
	public String readString(ReadObjectType in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readUTF(); }, in);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <ObjectType> ObjectType readObject(ReadObjectType in) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_ObjectInputStream == null) {
				m_ObjectInputStreamDispatcher = new InputStreamDispatcher();
				try {
					m_ObjectInputStream = new ObjectInputStream(m_ObjectInputStreamDispatcher);
				} catch (IOException e) {
					throw new PacketIOException(e);
				}
			}
		}
		
		synchronized (m_ObjectInputStreamDispatcher) {
			m_ObjectInputStreamDispatcher.putStream(in);
			try {
				return (ObjectType)m_ObjectInputStream.readObject();
			} catch (IOException|ClassNotFoundException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_ObjectInputStreamDispatcher.putStream(null);
			}
		}
	}
}
