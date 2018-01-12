package packet.binary;

import java.io.InputStream;
import java.io.OutputStream;

import packet.IOMethodType;
import packet.IOMethodType.MethodType;

public final class BinarySerialize {
	@IOMethodType(type=MethodType.read, general=false)
	public final boolean readBoolean(InputStream in) {
		return true;
	}
	@IOMethodType(type=MethodType.read, general=false)
	public final Boolean readLangBoolean(InputStream in) {
		return (Boolean)readBoolean(in);
	}
	@IOMethodType(type=MethodType.write, general=false)
	public final void writeBoolean(OutputStream out, boolean inst) {
	}
	@IOMethodType(type=MethodType.write, general=false)
	public final void writeLangBoolean(OutputStream out, boolean inst) {
		writeBoolean(out, (boolean)inst);
	}
}
