package io.github.sainttheana;
import java.util.List;
import java.util.ArrayList;

public class Text
{
	
	public Text(int maxContentSize){
		contents =new LimitedList<String>(maxContentSize);
	}
	
	private int currentIndex=0;
	
	private LimitedList<String> contents ;//=new LimitedList<String>();

	public void setCurrentIndex(int currentIndex)
	{
		this.currentIndex = currentIndex;
	}

	public int getCurrentIndex()
	{
		return currentIndex;
	}
	
	public String getLastLine()
	{
		if(contents.size()==0){
		    return "";
		}
		return contents.get(contents.size()-1);
	}

	public List<String> getLastLines(int count)
	{
		List<String> t=new ArrayList<String>();
		int size=contents.size();
		int firstLine=0;
		for(int i=0;i<=count;i++){
			if(size-1-i<0){
				break;
			}
			t.add(0,contents.get(size-1-i));
			firstLine=size-1-i;
		}
		currentIndex=firstLine;
		return t;
	}

	public void setLast(String string)
	{
		//System.err.println("setLast "+string);
		contents.set(contents.size() - 1, string);
	}
	
	public void appendLine(String line){
		//System.err.println("appendLine "+line);
		contents.add(line);
	}
	
	public List<String> getLines(int start,int count){
		if(start>=contents.size()){
			//throw new IllegalStateException("start > contents size");
			start=contents.size()-1;
		}
		List<String> t=new ArrayList<String>();
		for(int i=0;i<count;i++){
			if(start+i>=contents.size()){
				break;
			}
			t.add(contents.get(start+i));
		}
		currentIndex=start;
		return t;
	}
	
	
	public int getSize(){
		return contents.size();
	}
}
