package packet.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import packet.IOMethodInformation;
import packet.IOMethodInformation.MethodType;
import packet.InputStreamDispatcher;
import packet.OutputStreamDispatcher;
import packet.PacketException;
import packet.PacketIOException;
import packet.Registry;

/**
 * сериалайзер для вывода данных в бинарный поток
 * @author Ilya Sokolov
 */
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
		T read(DataInputStream data_in, Registry.ReadAccessor racc) throws IOException;
	}
	
	private final <T> T readData(readData<T> read, InputStream in, Registry.ReadAccessor racc) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataInputStream == null) {
				m_DataInputStreamDispatcher = new InputStreamDispatcher();
				m_DataInputStream = new DataInputStream(m_DataInputStreamDispatcher);
			}
		}
		
		synchronized (m_DataInputStreamDispatcher) {
			m_DataInputStreamDispatcher.putStream(in);
			try {
				return read.read(m_DataInputStream, racc);
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
		void write(DataOutputStream data_out, Registry.WriteAccessor wacc, T instance) throws IOException;
	}
	
	private final <T> void writeData(writeData<T> write, OutputStream out, Registry.WriteAccessor wacc, T instance) throws PacketIOException {
		synchronized (m_SynchObject) {
			if(m_DataOutputStream == null) {
				m_DataOutputStreamDispatcher = new OutputStreamDispatcher();
				m_DataOutputStream = new DataOutputStream(m_DataOutputStreamDispatcher);
			}
		}
		
		synchronized (m_DataOutputStreamDispatcher) {
			m_DataOutputStreamDispatcher.putStream(out);
			try {
				write.write(m_DataOutputStream, wacc, instance);
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
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final boolean readBoolean(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readBoolean(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Boolean readLangBoolean(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (Boolean)readBoolean(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeBoolean(OutputStream write, Registry.WriteAccessor wacc, boolean inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Boolean instance) -> { data_out.writeBoolean(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangBoolean(OutputStream write, Registry.WriteAccessor wacc, Boolean inst) throws PacketIOException {
		writeBoolean(write, wacc, (Boolean)inst);
	}

	/*
	 * char
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final char readCharacter(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readChar(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Character readLangCharacter(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (Character)readCharacter(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeCharacter(OutputStream write, Registry.WriteAccessor wacc, char inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Character instance) -> { data_out.writeChar(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangCharacter(OutputStream write, Registry.WriteAccessor wacc, Character inst) throws PacketIOException {
		writeCharacter(write, wacc, (char)inst);
	}

	/*
	 * byte
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final byte readByte(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readByte(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Byte readLangByte(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (byte)readByte(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeByte(OutputStream write, Registry.WriteAccessor wacc, byte inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Byte instance) -> { data_out.writeChar(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangByte(OutputStream write, Registry.WriteAccessor wacc, Byte inst) throws PacketIOException {
		writeByte(write, wacc, (byte)inst);
	}
	
	/*
	 * short
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final short readShort(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readShort(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Short readLangShort(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (short)readShort(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeShort(OutputStream write, Registry.WriteAccessor wacc, short inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Short instance) -> { data_out.writeShort(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangByte(OutputStream write, Registry.WriteAccessor wacc, Short inst) throws PacketIOException {
		writeShort(write, wacc, (short)inst);
	}
	
	/*
	 * int
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final int readInteger(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readInt(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Integer readLangInteger(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (Integer)readInteger(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeInteger(OutputStream write, Registry.WriteAccessor wacc, int inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Integer instance) -> { data_out.writeInt(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangInteger(OutputStream write, Registry.WriteAccessor wacc, Integer inst) throws PacketIOException {
		writeInteger(write, wacc, (int)inst);
	}
	
	/*
	 * long
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final long readLong(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readLong(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Long readLangLong(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (Long)readLong(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLong(OutputStream write, Registry.WriteAccessor wacc, long inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Long instance) -> { data_out.writeLong(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangLong(OutputStream write, Registry.WriteAccessor wacc, Long inst) throws PacketIOException {
		writeLong(write, wacc, (long)inst);
	}
	
	/*
	 * float
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final float readFloat(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readFloat(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Float readLangFloat(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (float)readLong(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeFloat(OutputStream write, Registry.WriteAccessor wacc, float inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Float instance) -> { data_out.writeFloat(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangFloat(OutputStream write, Registry.WriteAccessor wacc, Float inst) throws PacketIOException {
		writeFloat(write, wacc, (float)inst);
	}
	
	/*
	 * double
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final double readDouble(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readDouble(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Double readLangDouble(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return (Double)readDouble(read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeDouble(OutputStream write, Registry.WriteAccessor wacc, double inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, Double instance) -> { data_out.writeDouble(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangFloat(OutputStream write, Registry.WriteAccessor wacc, Double inst) throws PacketIOException {
		writeDouble(write, wacc, (double)inst);
	}
	
	/*
	 * String
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final String readString(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData((DataInputStream data_in, Registry.ReadAccessor r_acc) -> { return data_in.readUTF(); }, read, racc);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeString(OutputStream write, Registry.WriteAccessor wacc, String inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, String instance) -> { data_out.writeUTF(instance); }, write, wacc, inst);
	}
	
	/*
	 * byte[]???????????????????????
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final byte[] readByteArray(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		return readData(
				(DataInputStream data_in, Registry.ReadAccessor r_acc) -> {
					int _len = 0;
					try {
						_len = readInteger(read, r_acc);
					}
					catch(PacketException e) {
						throw (IOException)e.getCause();
					}
					byte[] _buff = new byte[_len];
					if(data_in.read(_buff) != _len) {
						throw new IOException("incorrect read byte count");
					}
					return _buff;
				}, read, racc);
	}
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Byte[] readLangByteArray(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
		byte[] _rb = readByteArray(read, racc);
		Byte[] _ab = new Byte[_rb.length];
		for(int i = 0; i < _rb.length; ++i) { _ab[i] = (Byte)_rb[i]; }
		return (Byte[])_ab;
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeByteArray(OutputStream write, Registry.WriteAccessor wacc, byte[] inst) throws PacketIOException {
		writeData((DataOutputStream data_out, Registry.WriteAccessor w_acc, byte[] instance) -> { data_out.write(instance); }, write, wacc, inst);
	}
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeLangByteArray(OutputStream write, Registry.WriteAccessor wacc, Byte[] inst) throws PacketIOException {
		byte[] _ab = new byte[inst.length];
		for(int i = 0; i < inst.length; ++i) { _ab[i] = (byte)inst[i]; }
		writeByteArray(write, wacc, (byte[])_ab);
	}
	
	/*
	 * Object
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=false)
	public final Object readObject(InputStream read, Registry.ReadAccessor racc) throws PacketIOException {
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
	@IOMethodInformation(type=MethodType.write, universal=false)
	public final void writeObject(OutputStream write, Registry.WriteAccessor wacc, Object inst) throws PacketIOException {
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