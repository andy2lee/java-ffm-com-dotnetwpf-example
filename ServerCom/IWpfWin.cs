using System.Runtime.InteropServices;

namespace ServerCom;

[ComVisible(true)]
[Guid("8C50A2D4-34D3-4EC3-A540-41A43F668E52")]
[InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
public interface IWpfWin
{
	int wpfwin(int x, int y);
}
