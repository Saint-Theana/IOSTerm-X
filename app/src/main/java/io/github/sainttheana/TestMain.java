package io.github.sainttheana;
import java.util.Scanner;

public class TestMain
{

	private static BasicTerminal terminal;

	private static TestMain.MyInputReader inputReader;




    public static void main(String[] args) throws Exception{
        
		inputReader=new MyInputReader();
		terminal = new BasicTerminal(inputReader);
		terminal.process();
    
    }
	
	
	
	private static class MyInputReader implements BasicTerminal.InputReader
	{
		@Override
		public void read(String input)
		{
			System.out.println("read input!"+input);
			Scanner s=new Scanner(System.in);
			System.out.println(s.next());
		}
	}


}
