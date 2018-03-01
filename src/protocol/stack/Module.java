package protocol.stack;

public interface Module <InT, OutT> {
	InT read(OutT rd);
	OutT write(InT wr);
}
