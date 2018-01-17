package packet.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import packet.IOMethodInfo;
import packet.IOMethodInfo.MethodType;

public final class BinarySerialize {
	private interface ReadDispatcher {
		Object read(InputStream read);
	}
	private interface WriteDispatcher {
		void write(OutputStream write, Object inst);
	}
	
	private final Object gSynchObject = new Object();
	private DataInputStream gDataInputStream = null;
	private DataOutputStream gDataOutputStream = null;
	
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final boolean readBoolean(InputStream read) {
		return true;
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Boolean readLangBoolean(InputStream read) {
		return (Boolean)readBoolean(read);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeBoolean(OutputStream write, boolean inst) {
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangBoolean(OutputStream write, boolean inst) {
		writeBoolean(write, (boolean)inst);
	}
}
