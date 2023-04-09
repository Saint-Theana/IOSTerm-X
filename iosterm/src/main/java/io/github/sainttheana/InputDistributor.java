package io.github.sainttheana;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class InputDistributor extends Thread
{
	public static LinkedList<InputReader> readers=new LinkedList<InputReader>();

	public static List<InputReader> permanentReaders=new ArrayList<InputReader>();
	
	public static void registerHighPriority(InputReader reader){
		if(readers.contains(reader)){
			return;
		}
		readers.addFirst(reader);
	}

	public static void registerLowPriority(InputReader reader){
		if(readers.contains(reader)){
			return;
		}
		readers.addLast(reader);
	}
	
	public static void registerAlways(InputReader reader){
		if(permanentReaders.contains(reader)){
			return;
		}
		permanentReaders.add(reader);
	}
	

	public static void unRegister(InputReader reader){
		readers.remove(reader);
		permanentReaders.remove(reader);
	}

	public interface InputReader{
		void read(String string);
	}

	@Override
	public void run()
	{
		Scanner scaner=new Scanner(System.in);
		while(true){
			String input =scaner.nextLine();
			for(InputReader reader:permanentReaders){
				reader.read(input);
			}
			if(readers.size()>0){
			    readers.peekFirst().read(input);
			}
		}
	}
}
