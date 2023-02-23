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
		for(byte b:in){
			bytes.add(b);
		}
	}

	public void clear()
	{
		bytes.clear();
	}

	public void deleteLast()
	{
		if(bytes.size()==0){
			return;
		}
		bytes.remove(bytes.size()-1);
	}
	
	public void write(byte a){
		bytes.add(a);
	}
	
	@Override
	public synchronized int read() throws IOException
	{
		 if(bytes.size()==0){
			 return -1;
		 }
		 return bytes.remove(0);
	}
	
}
