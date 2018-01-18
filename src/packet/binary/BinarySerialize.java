package packet.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

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
	
	/*
	 * boolean
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final boolean readBoolean(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readBoolean(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Boolean readLangBoolean(InputStream read) throws PacketIOException {
		return (Boolean)readBoolean(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeBoolean(OutputStream write, boolean inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Boolean instance) -> { data_out.writeBoolean(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangBoolean(OutputStream write, Boolean inst) throws PacketIOException {
		writeBoolean(write, (Boolean)inst);
	}

	/*
	 * char
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final char readCharacter(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readChar(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Character readLangCharacter(InputStream read) throws PacketIOException {
		return (Character)readCharacter(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeCharacter(OutputStream write, char inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Character instance) -> { data_out.writeChar(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangCharacter(OutputStream write, Character inst) throws PacketIOException {
		writeCharacter(write, (char)inst);
	}

	/*
	 * byte
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final byte readByte(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readByte(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Byte readLangByte(InputStream read) throws PacketIOException {
		return (byte)readByte(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeByte(OutputStream write, byte inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Byte instance) -> { data_out.writeChar(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangByte(OutputStream write, Byte inst) throws PacketIOException {
		writeByte(write, (byte)inst);
	}
	
	/*
	 * short
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final short readShort(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readShort(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Short readLangShort(InputStream read) throws PacketIOException {
		return (short)readShort(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeShort(OutputStream write, short inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Short instance) -> { data_out.writeShort(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangByte(OutputStream write, Short inst) throws PacketIOException {
		writeShort(write, (short)inst);
	}
	
	/*
	 * int
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final int readInteger(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readInt(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Integer readLangInteger(InputStream read) throws PacketIOException {
		return (Integer)readInteger(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeInteger(OutputStream write, int inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Integer instance) -> { data_out.writeInt(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangInteger(OutputStream write, Integer inst) throws PacketIOException {
		writeInteger(write, (int)inst);
	}
	
	/*
	 * long
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final long readLong(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readLong(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Long readLangLong(InputStream read) throws PacketIOException {
		return (Long)readLong(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLong(OutputStream write, long inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Long instance) -> { data_out.writeLong(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangLong(OutputStream write, Long inst) throws PacketIOException {
		writeLong(write, (long)inst);
	}
	
	/*
	 * float
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final float readFloat(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readFloat(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Float readLangFloat(InputStream read) throws PacketIOException {
		return (float)readLong(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeFloat(OutputStream write, float inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Float instance) -> { data_out.writeFloat(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangFloat(OutputStream write, Float inst) throws PacketIOException {
		writeFloat(write, (float)inst);
	}
	
	/*
	 * double
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final double readDouble(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readDouble(); }, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Double readLangDouble(InputStream read) throws PacketIOException {
		return (Double)readDouble(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeDouble(OutputStream write, double inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Double instance) -> { data_out.writeDouble(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangFloat(OutputStream write, Double inst) throws PacketIOException {
		writeDouble(write, (double)inst);
	}
	
	/*
	 * String
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final String readString(InputStream read) throws PacketIOException {
		return readData((DataInputStream data_in) -> { return data_in.readUTF(); }, read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeString(OutputStream write, String inst) throws PacketIOException {
		writeData((DataOutputStream data_out, String instance) -> { data_out.writeUTF(instance); }, write, inst);
	}
	
	/*
	 * byte[]
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final byte[] readByteArray(InputStream read) throws PacketIOException {
		return readData(
				(DataInputStream data_in) -> {
					int _len = readInteger(read);
					byte[] _buff = new byte[_len];
					if(data_in.read(_buff) != _len) {
						throw new PacketIOException("incorrect read byte count");
					}
					return _buff;
				}, read);
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Byte[] readLangByteArray(InputStream read) throws PacketIOException {
		byte[] _rb = readByteArray(read);
		Object _ab = Array.newInstance(Byte.class, _rb.length);
		for(int i = 0; i < _rb.length; ++i) { Array.set(_ab, i, _rb[i]); }
		return (Byte[])_ab;
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeByteArray(OutputStream write, byte[] inst) throws PacketIOException {
		writeData((DataOutputStream data_out, byte[] instance) -> { data_out.write(instance); }, write, inst);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangByteArray(OutputStream write, Byte[] inst) throws PacketIOException {
		byte[] _buff = new byte[inst.length];
		Object _ab = Array.newInstance(byte.class, inst.length);
		for(int i = 0; i < inst.length; ++i) { Array.set(_ab, i, inst[i]); }
		writeByteArray(write, (byte[])_ab);
	}
	
	/*
	 * Object
	 */
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Object readObject(InputStream read) throws PacketIOException {
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
			m_ObjectInputStreamDispatcher.putStream(read);
			try {
				return m_ObjectInputStream.readObject();
			} catch (IOException|ClassNotFoundException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_ObjectInputStreamDispatcher.putStream(null);
			}
		}
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeObject(OutputStream write, Object inst) throws PacketIOException {
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
			m_ObjectOutputStreamDispatcher.putStream(write);
			try {
				m_ObjectOutputStream.writeObject(inst);;
			} catch (IOException e) {
				throw new PacketIOException(e);
			}
			finally {
				m_ObjectOutputStreamDispatcher.putStream(null);
			}
		}
	}
}