package packet.binary;

import java.io.InputStream;
import java.io.OutputStream;

import packet.IOMethodInfo;
import packet.IOMethodInfo.MethodType;

public final class BinarySerialize {
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final boolean readBoolean(InputStream in) {
		return true;
	}
	@IOMethodInfo(type=MethodType.read, universal=false)
	public final Boolean readLangBoolean(InputStream in) {
		return (Boolean)readBoolean(in);
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeBoolean(OutputStream out, boolean inst) {
	}
	@IOMethodInfo(type=MethodType.write, universal=false)
	public final void writeLangBoolean(OutputStream out, boolean inst) {
		writeBoolean(out, (boolean)inst);
	}
}
