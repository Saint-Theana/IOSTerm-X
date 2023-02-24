package io.github.sainttheana;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TerminalInputStream extends InputStream
{
	public List<Byte> bytes=new ArrayList<>();

	public void wrap(byte[] in)
	{
		for (byte b:in)
		{
			bytes.add(b);
		}
	}

	@Override
	public int available() throws IOException
	{
		return bytes.size();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		while (bytes.size() == 0)
		{
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		int length =available() > b.length ?b.length: available();
		for (int i=0;i < length;i++)
		{
			b[i] = bytes.remove(0);
		}
		return length;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		while (bytes.size() == 0)
		{
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		int length =available() > b.length ?b.length: available();
		for (int i=0;i < length;i++)
		{
			b[off + i] = bytes.remove(0);
		}
		return length;
	}







	public void clear()
	{
		bytes.clear();
	}

	public void deleteLast()
	{
		if (bytes.size() == 0)
		{
			return;
		}
		bytes.remove(bytes.size() - 1);
	}

	public void write(byte a)
	{
		bytes.add(a);
	}

	@Override
	public int read() throws IOException
	{
		while (bytes.size() == 0)
		{
			try
			{
				Thread.currentThread().sleep(100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return bytes.remove(0);
	}

}
