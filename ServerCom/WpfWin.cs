using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Controls;

// dotnet new classlib -n ServerCom
// dotnet build -c Release

namespace ServerCom;

[ComVisible(true)]
[Guid("7FBC4AEE-5AF0-4D81-A0F6-8AA4A0A82A02")]
[ClassInterface(ClassInterfaceType.None)]
[ProgId("ServerCom.WpfWin")]
public class WpfWin: IWpfWin
{
	public int wpfwin(int x, int y)
	{
		Window w = new Window();
		w.Title = $"Add({x}, {y})";

		Button b = new Button();
		b.Content = "Click Me";
		b.Width = 100;
		b.Height = 40;

		w.Content = b;
		w.ShowDialog();

		return x + y;
	}
}
