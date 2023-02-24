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
		System.out.println("输入帐号");
		Scanner sc=new Scanner(System.in);
		String account=sc.next();
		System.out.println("ffff "+account);
		
    }
	
	
	
	private static class MyInputReader implements BasicTerminal.InputReader
	{
		@Override
		public void read(String input)
		{
			//System.out.println("read input!"+input);
			
		}
	}


}
