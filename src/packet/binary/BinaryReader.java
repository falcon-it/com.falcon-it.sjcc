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
public final class BinaryReader implements Reader<InputStream> {
	private final DataInputStream m_DataStream;
	private final InputStreamDispatcher m_StreamDispatcher;
	
	public BinaryReader() {
		m_StreamDispatcher = new InputStreamDispatcher();
		m_DataStream = new DataInputStream(m_StreamDispatcher);
	}

	/*
	 * read byte array
	 */
	
	private final void readByteArray(InputStream in, byte[] b, int off, int len) throws PacketIOException {
		try {
			synchronized (m_DataStream) {
				m_StreamDispatcher.putStream(in);
				try {
					m_DataStream.read(b, off, len);
				}
				finally {
					m_StreamDispatcher.putStream(null);
				}
			}
		} catch (IOException e) {
			throw new PacketIOException(e);
		}
	}
	
	@Override
	public void readBytes(InputStream in, byte[] b) throws PacketIOException {
		readByteArray(in, b, 0, b.length);
	}
	
	@Override
	public void readBytes(InputStream in, byte[] b, int off, int len) throws PacketIOException {
		readByteArray(in, b, off, len);
	}
	
	/*
	 * read Data
	 */
	
	private interface readData <T> {
		T read(DataInputStream data_in) throws IOException;
	}
	
	private final <T> T readData(readData<T> read, InputStream in) throws PacketIOException {
		try {
			synchronized (m_DataStream) {
				m_StreamDispatcher.putStream(in);
				try {
					return read.read(m_DataStream);
				}
				finally {
					m_StreamDispatcher.putStream(null);
				}
			}
		} catch (IOException e) {
			throw new PacketIOException(e);
		}
	}
	
	@Override
	public boolean readBoolean(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readBoolean(); }, in);
	}
	
	@Override
	public byte readByte(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readByte(); }, in);
	}
	
	@Override
	public char readChar(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readChar(); }, in);
	}
	
	@Override
	public double readDouble(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readDouble(); }, in);
	}
	
	@Override
	public float readFloat(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readFloat(); }, in);
	}
	
	@Override
	public int readInt(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readInt(); }, in);
	}
	
	@Override
	public long readLong(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readLong(); }, in);
	}
	
	@Override
	public short readShort(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readShort(); }, in);
	}
	
	@Override
	public String readString(InputStream in) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readUTF(); }, in);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <ObjectType> ObjectType readObject(InputStream in) throws PacketIOException {
		try {
			return (ObjectType)(new ObjectInputStream(in)).readObject();
		} catch (IOException|ClassNotFoundException e) {
			throw new PacketIOException(e);
		}
	}
}
