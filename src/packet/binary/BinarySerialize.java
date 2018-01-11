package packet.binary;

import java.io.InputStream;

import packet.IOMethodType;
import packet.IOMethodType.MethodType;

public final class BinarySerialize {
	@IOMethodType(type=MethodType.read, general=false)
	public final boolean readBoolean(InputStream in) {
		return true;
	}
}
