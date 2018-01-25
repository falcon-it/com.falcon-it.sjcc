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
public final class BinaryWriter<WriteObjectType extends OutputStream> implements Writer<WriteObjectType> {
	private final Object m_SynchObject = new Object();
	private DataOutputStream m_DataOutputStream = null;
	private OutputStreamDispatcher m_DataOutputStreamDispatcher = null;
	private ObjectOutputStream m_ObjectOutputStream = null;
	private OutputStreamDispatcher m_ObjectOutputStreamDispatcher = null;

	/*
	 * write byte array
	 */
	
	private final void writeByteArray(OutputStream out, byte[] b, int off, int len) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataOutputStream == null) {
				m_DataOutputStreamDispatcher = new OutputStreamDispatcher();
				m_DataOutputStream = new DataOutputStream(m_DataOutputStreamDispatcher);
			}
		}
		
		synchronized (m_DataOutputStreamDispatcher) {
			m_DataOutputStreamDispatcher.putStream(out);
			try {
				m_DataOutputStream.write(b, off, len);
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataOutputStreamDispatcher.putStream(null);
			}
		}
	}
	
	@Override
	public void write(WriteObjectType out, byte[] b) throws PacketIOException {
		writeByteArray(out, b, 0, b.length);
	}
	
	@Override
	public void write(WriteObjectType out, byte[] b, int off, int len) throws PacketIOException {
		writeByteArray(out, b, off, len);
	}
	
	/*
	 * write Data
	 */
	
	private interface writeData <T> {
		void write(DataOutputStream data_out, T instance) throws IOException;
	}
	
	private final <T> void writeData(writeData<T> write, OutputStream out, T instance) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataOutputStream == null) {
				m_DataOutputStreamDispatcher = new OutputStreamDispatcher();
				m_DataOutputStream = new DataOutputStream(m_DataOutputStreamDispatcher);
			}
		}
		
		synchronized (m_DataOutputStreamDispatcher) {
			m_DataOutputStreamDispatcher.putStream(out);
			try {
				write.write(m_DataOutputStream, instance);
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataOutputStreamDispatcher.putStream(null);
			}
		}
	}
	
	@Override
	public void writeBoolean(WriteObjectType out, boolean v) throws PacketIOException {
		writeData((DataOutputStream data_out, Boolean data_v) -> { data_out.writeBoolean(data_v); }, out, v);
	}
	
	@Override
	public void writeByte(WriteObjectType out, byte v) throws PacketIOException {
		writeData((DataOutputStream data_out, Byte data_v) -> { data_out.writeByte(data_v); }, out, v);
	}
	
	@Override
	public void writeChar(WriteObjectType out, char v) throws PacketIOException {
		writeData((DataOutputStream data_out, Character data_v) -> { data_out.writeChar(data_v); }, out, v);
	}
	
	@Override
	public void writeDouble(WriteObjectType out, double v) throws PacketIOException {
		writeData((DataOutputStream data_out, Double data_v) -> { data_out.writeDouble(data_v); }, out, v);
	}
	
	@Override
	public void writeFloat(WriteObjectType out, float v) throws PacketIOException {
		writeData((DataOutputStream data_out, Float data_v) -> { data_out.writeFloat(data_v); }, out, v);
	}
	
	@Override
	public void writeInt(WriteObjectType out, int v) throws PacketIOException {
		writeData((DataOutputStream data_out, Integer data_v) -> { data_out.writeInt(data_v); }, out, v);
	}
	
	@Override
	public void writeLong(WriteObjectType out, long v) throws PacketIOException {
		writeData((DataOutputStream data_out, Long data_v) -> { data_out.writeLong(data_v); }, out, v);
	}
	
	@Override
	public void writeShort(WriteObjectType out, short v) throws PacketIOException {
		writeData((DataOutputStream data_out, Short data_v) -> { data_out.writeShort(data_v); }, out, v);
	}
	
	@Override
	public void writeString(WriteObjectType out, String v) throws PacketIOException {
		writeData((DataOutputStream data_out, String data_v) -> { data_out.writeUTF(data_v); }, out, v);
	}
	
	@Override
	public void writeObject(WriteObjectType out, Object v) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_ObjectOutputStream == null) {
				m_ObjectOutputStreamDispatcher = new OutputStreamDispatcher();
				try {
					m_ObjectOutputStream = new ObjectOutputStream(m_ObjectOutputStreamDispatcher);
				} catch (IOException e) {
					throw new PacketIOException(e);
				}
			}
		}
		
		synchronized (m_ObjectOutputStreamDispatcher) {
			m_ObjectOutputStreamDispatcher.putStream(out);
			try {
				m_ObjectOutputStream.writeObject(v);;
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_ObjectOutputStreamDispatcher.putStream(null);
			}
		}
	}
}
