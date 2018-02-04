package packet.binary;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import packet.OutputStreamDispatcher;
import packet.PacketIOException;
import packet.Writer;

/**
 * реализация интерфейса писателя для бинароного вывода
 * @author Ilya Sokolov
 * @param <WriteObjectType>
 */
public final class BinaryWriter implements Writer<OutputStream> {
	private final DataOutputStream m_DataStream;
	private final OutputStreamDispatcher m_StreamDispatcher;
	
	public BinaryWriter() {
		m_StreamDispatcher = new OutputStreamDispatcher();
		m_DataStream = new DataOutputStream(m_StreamDispatcher);
	}
	
	/*
	 * write byte array
	 */
	
	private final void writeByteArray(OutputStream out, byte[] b, int off, int len) throws PacketIOException {
		try {
			synchronized (m_DataStream) {
				m_StreamDispatcher.putStream(out);
				try {
					m_DataStream.write(b, off, len);
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
	public void write(OutputStream out, byte[] b) throws PacketIOException {
		writeByteArray(out, b, 0, b.length);
	}
	
	@Override
	public void write(OutputStream out, byte[] b, int off, int len) throws PacketIOException {
		writeByteArray(out, b, off, len);
	}
	
	/*
	 * write Data
	 */
	
	private interface writeData <T> {
		void write(DataOutputStream data_out, T instance) throws IOException;
	}
	
	private final <T> void writeData(writeData<T> write, OutputStream out, T instance) throws PacketIOException {
		try {
			synchronized (m_DataStream) {
				m_StreamDispatcher.putStream(out);
				try {
					write.write(m_DataStream, instance);
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
	public void writeBoolean(OutputStream out, boolean v) throws PacketIOException {
		writeData((DataOutputStream data_out, Boolean data_v) -> { data_out.writeBoolean(data_v); }, out, v);
	}
	
	@Override
	public void writeByte(OutputStream out, byte v) throws PacketIOException {
		writeData((DataOutputStream data_out, Byte data_v) -> { data_out.writeByte(data_v); }, out, v);
	}
	
	@Override
	public void writeChar(OutputStream out, char v) throws PacketIOException {
		writeData((DataOutputStream data_out, Character data_v) -> { data_out.writeChar(data_v); }, out, v);
	}
	
	@Override
	public void writeDouble(OutputStream out, double v) throws PacketIOException {
		writeData((DataOutputStream data_out, Double data_v) -> { data_out.writeDouble(data_v); }, out, v);
	}
	
	@Override
	public void writeFloat(OutputStream out, float v) throws PacketIOException {
		writeData((DataOutputStream data_out, Float data_v) -> { data_out.writeFloat(data_v); }, out, v);
	}
	
	@Override
	public void writeInt(OutputStream out, int v) throws PacketIOException {
		writeData((DataOutputStream data_out, Integer data_v) -> { data_out.writeInt(data_v); }, out, v);
	}
	
	@Override
	public void writeLong(OutputStream out, long v) throws PacketIOException {
		writeData((DataOutputStream data_out, Long data_v) -> { data_out.writeLong(data_v); }, out, v);
	}
	
	@Override
	public void writeShort(OutputStream out, short v) throws PacketIOException {
		writeData((DataOutputStream data_out, Short data_v) -> { data_out.writeShort(data_v); }, out, v);
	}
	
	@Override
	public void writeString(OutputStream out, String v) throws PacketIOException {
		writeData((DataOutputStream data_out, String data_v) -> { data_out.writeUTF(data_v); }, out, v);
	}
	
	@Override
	public void writeObject(OutputStream out, Object v) throws PacketIOException {
		try {
			(new ObjectOutputStream(out)).writeObject(v);;
		} catch (IOException e) {
			throw new PacketIOException(e);
		}
	}
}
