/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 USA

 Please contact Saint-Theana by email the.winter.will.come@gmail.com if you need
 additional information or have any questions
 */
package io.github.sainttheana;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ArrayBlockingQueue;

public class TerminalInputStream extends InputStream
{
	public ArrayBlockingQueue<Byte> bytes=new ArrayBlockingQueue<>(2048);

	public void wrap(byte[] in)
	{
		bytes.clear();
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
		int length =available() > b.length ?b.length: available();
		for (int i=0;i < length;i++)
		{
			try
			{
				b[i] = bytes.take();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return length;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		while (bytes.size() == 0)
		{

		}
		int length =available() > b.length ?b.length: available();
		for (int i=0;i < length;i++)
		{
			try
			{
				b[off + i] = bytes.take();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
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
		try
		{
			return bytes.take();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

}
