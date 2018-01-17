package packet.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import packet.IOMethodInfo;
import packet.IOMethodInfo.MethodType;
import packet.InputStreamDispatcher;
import packet.OutputStreamDispatcher;
import packet.PacketIOException;

public final class BinarySerialize {
	private final Object m_SynchObject = new Object();
	private DataInputStream m_DataInputStream = null;
	private InputStreamDispatcher m_DataInputStreamDispatcher = null;
	private DataOutputStream m_DataOutputStream = null;
	private OutputStreamDispatcher m_DataOutputStreamDispatcher = null;
	private ObjectInputStream m_ObjectInputStream = null;
	private InputStreamDispatcher m_ObjectInputStreamDispatcher = null;
	private ObjectOutputStream m_ObjectOutputStream = null;
	private OutputStreamDispatcher m_ObjectOutputStreamDispatcher = null;
	
	private final void initDataInputStream() {
		synchronized (m_SynchObject) {
			if(m_DataInputStream == null) {
				m_DataInputStreamDispatcher = new InputStreamDispatcher();
				m_DataInputStream = new DataInputStream(m_DataInputStreamDispatcher);
			}
		}
	}
	
	private final void initDataOutputStream() {
		synchronized (m_SynchObject) {
			if(m_DataOutputStream == null) {
				m_DataOutputStreamDispatcher = new OutputStreamDispatcher();
				m_DataOutputStream = new DataOutputStream(m_DataOutputStreamDispatcher);
			}
		}
	}
	
	private final void initObjectInputStream() throws IOException {
		synchronized (m_SynchObject) {
			if(m_ObjectInputStream == null) {
				m_ObjectInputStreamDispatcher = new InputStreamDispatcher();
				m_ObjectInputStream = new ObjectInputStream(m_ObjectInputStreamDispatcher);
			}
		}
	}
	
	private final void initObjectOutputStream() throws IOException {
		synchronized (m_SynchObject) {
			if(m_ObjectOutputStream == null) {
				m_ObjectOutputStreamDispatcher = new OutputStreamDispatcher();
				m_ObjectOutputStream = new ObjectOutputStream(m_ObjectOutputStreamDispatcher);
			}
		}
	}
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final boolean readBoolean(InputStream read) throws PacketIOException {
		initDataInputStream();
		synchronized (m_DataInputStreamDispatcher) {
			m_DataInputStreamDispatcher.putStream(read);
			try {
				return m_DataInputStream.readBoolean();
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataInputStreamDispatcher.putStream(null);
			}
		}
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Boolean readLangBoolean(InputStream read) throws PacketIOException {
		return (Boolean)readBoolean(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeBoolean(OutputStream write, boolean inst) throws PacketIOException {
		initDataOutputStream();
		synchronized (m_DataOutputStreamDispatcher) {
			m_DataOutputStreamDispatcher.putStream(write);
			try {
				m_DataOutputStream.writeBoolean(inst);
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_DataOutputStreamDispatcher.putStream(null);
			}
		}
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangBoolean(OutputStream write, boolean inst) throws PacketIOException {
		writeBoolean(write, (boolean)inst);
	}
}
