package io.github.sainttheana;
import java.util.List;
import java.util.ArrayList;

public class Text
{
	private LimitedList<String> contents =new LimitedList<String>(2000);

	public List<String> getLastLines(int count)
	{
		List<String> t=new ArrayList<String>();
		int size=contents.size();
		for(int i=0;i<=count;i++){
			if(size-1-i<0){
				break;
			}
			t.add(0,contents.get(size-1-i));
		}
		return t;
	}

	public void setLast(String string)
	{
		contents.set(contents.size() - 1, string);
	}
	
	public void appendLine(String line){
		contents.add(line);
	}
	
	public List<String> getLines(int start,int count){
		if(start>=contents.size()){
			throw new IllegalStateException("start > contents size");
		}
		List<String> t=new ArrayList<String>();
		for(int i=0;i<count;i++){
			if(start+i>=contents.size()){
				break;
			}
			t.add(contents.get(start+i));
		}
		return t;
	}
	
	
	public int getSize(){
		return contents.size();
	}
	
}
