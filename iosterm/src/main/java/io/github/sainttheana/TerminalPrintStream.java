package io.github.sainttheana;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public class TerminalPrintStream extends PrintStream
{

	private FrameTerminalScreen screen;
	
	public TerminalPrintStream(FrameTerminalScreen screen)
	{
		super(System.out);
		this.screen=screen;
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		screen.print(new String(b));
	}

	@Override
	public void write(byte[] buf, int off, int len)
	{
		screen.print(new String(buf, off, len));
	}
	
	
	
	
	

	@Override
	public void print(Object obj)
	{
		screen.print(obj.toString());
	}

	@Override
	public void print(char c)
	{
		screen.print(c+"");
	}

	@Override
	public void print(double d)
	{
		screen.print(d+"");
	}

	@Override
	public void print(boolean b)
	{
		screen.print(b+"");
	}

	@Override
	public void print(String s)
	{
		screen.print(s+"");
	}

	@Override
	public void println(Object x)
	{
		screen.println(x+"");
	}

	@Override
	public void println(int x)
	{
		screen.println(x+"");
	}

	@Override
	public void print(float f)
	{
		screen.print(f+"");
	}

	@Override
	public void print(int i)
	{
		screen.print(i+"");
	}

	@Override
	public void println(String x)
	{
		screen.println(x+"");
	}

	@Override
	public void println(float x)
	{
		screen.println(x+"");
	}

	@Override
	public void println(boolean x)
	{
		screen.println(x+"");
	}

	@Override
	public void println(char x)
	{
		screen.println(x+"");
	}

	@Override
	public void println(long x)
	{
		screen.println(x+"");
	}

	@Override
	public PrintStream printf(Locale l, String format, Object[] args)
	{
		screen.print(String.format(l,format,args));
		return this;
		//return super.printf(l, format, args);
	}
//
	@Override
	public void print(long l)
	{
		screen.print(l+"");
	}

	@Override
	public void println(double x)
	{
		screen.println(x+"");
	}

	@Override
	public void println()
	{
		screen.println("");
	}

	@Override
	public void println(char[] x)
	{
		screen.println(new String(x));
	}

	@Override
	public void print(char[] s)
	{
		screen.print(new String(s));
	}

	@Override
	public PrintStream printf(String format, Object[] args)
	{
		screen.print(String.format(format,args));
		return this;
	}




};
