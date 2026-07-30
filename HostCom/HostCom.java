import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.UUID;
import static java.lang.foreign.ValueLayout.*;

// Install dll:   regsvr32 xxx.comhost.dll
// Uninstall dll: regsvr32 /u xxx.comhost.dll

public class HostCom {
	static MemorySegment guid(Arena arena, String guid_str) {
		UUID uuid = UUID.fromString(guid_str);
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		
		int d1 = (int)(msb >>> 32);
		short d2 = (short)(msb >>> 16);
		short d3 = (short)msb;
		byte[] d4 = new byte[8];

		for (int i = 0; i < 8; i++)
			d4[i] = (byte)(lsb >>> (56 - i * 8));

		MemorySegment p_guid = arena.allocate(16, 4);
		p_guid.set(JAVA_INT, 0, d1);
		p_guid.set(JAVA_SHORT, 4, d2);
		p_guid.set(JAVA_SHORT, 6, d3);
		
		for (int i = 0; i < 8; i++)
			p_guid.set(JAVA_BYTE, 8L + i, d4[i]);

		return p_guid;
	}

	static MemorySegment wide_string(Arena arena, String s) {
		MemorySegment p_str = arena.allocate((long) (s.length() + 1) * JAVA_CHAR.byteSize());
		for (int i = 0; i < s.length(); i++) {
			p_str.set(JAVA_CHAR, (long) i * JAVA_CHAR.byteSize(), s.charAt(i));
		}
		p_str.set(JAVA_CHAR, (long) s.length() * JAVA_CHAR.byteSize(), '\0');

		return p_str;
	}

	static MemorySegment deref_ptr(MemorySegment p) {
		return p.reinterpret(ADDRESS.byteSize()).get(ADDRESS, 0);
	}

	static MethodHandle vtableCall(Linker linker, MemorySegment obj, int slot, FunctionDescriptor fd) {
		MemorySegment vtbl = deref_ptr(obj);
		MemorySegment fn = vtbl.reinterpret((slot + 1L) * ADDRESS.byteSize()).get(ADDRESS, (long) slot * ADDRESS.byteSize());

		return linker.downcallHandle(fn, fd);
	}

	public static void main(String[] args) {
		Linker linker = Linker.nativeLinker();
		SymbolLookup ole32 = SymbolLookup.libraryLookup("ole32", Arena.global());
		MethodHandle CoInitializeEx = linker.downcallHandle(ole32.find("CoInitializeEx").orElseThrow(),
			FunctionDescriptor.of(
				JAVA_INT,
				ADDRESS,
				JAVA_INT));

		MethodHandle CLSIDFromProgID = linker.downcallHandle(ole32.find("CLSIDFromProgID").orElseThrow(),
			FunctionDescriptor.of(
				JAVA_INT,
				ADDRESS,
				ADDRESS));

		MethodHandle CoCreateInstance = linker.downcallHandle(ole32.find("CoCreateInstance").orElseThrow(),
			FunctionDescriptor.of(
				JAVA_INT,
				ADDRESS,
				ADDRESS,
				JAVA_INT,
				ADDRESS,
				ADDRESS));

		try (Arena arena = Arena.ofConfined()) {
			int hr = 0;

			hr = (int)CoInitializeEx.invoke(MemorySegment.NULL, 2);
			System.out.printf("CoInitializeEx: 0x%08X%n", hr);

			MemorySegment clsid = arena.allocate(16);
			hr = (int)CLSIDFromProgID.invoke(wide_string(arena, "ServerCom.WpfWin"), clsid);
			System.out.printf("CLSIDFromProgID: 0x%08X%n", hr);

			MemorySegment iidWpfWin = guid(arena, "8C50A2D4-34D3-4EC3-A540-41A43F668E52");
			MemorySegment ppv = arena.allocate(ADDRESS);
			hr = (int)CoCreateInstance.invoke(clsid, MemorySegment.NULL, 1, iidWpfWin, ppv);
			System.out.printf("CoCreateInstance: 0x%08X%n", hr);

			MemorySegment p_IWpfWin = ppv.get(ADDRESS, 0);
			System.out.printf("CoCreateInstance: 0x%X%n", p_IWpfWin.address());

			MethodHandle wpfwin = vtableCall(linker, p_IWpfWin, 3, // slot: 3
				FunctionDescriptor.of(
					JAVA_INT, // HRESULT
					ADDRESS,  // ppv addr
					JAVA_INT, // int
					JAVA_INT, // int
					ADDRESS));// int*

			MemorySegment res_out = arena.allocate(JAVA_INT);
			int ret = (int)wpfwin.invoke(p_IWpfWin, 3, 4, res_out);
			int res = res_out.get(JAVA_INT, 0);
			System.out.println(ret);
			System.out.println("res=" + res);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
