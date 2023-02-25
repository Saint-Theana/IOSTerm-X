package io.github.sainttheana;
import java.util.Scanner;

public class TestMain
{

	private static BasicTerminal terminal;

	private static TestMain.MyInputReader inputReader;




    public static void main(String[] args) throws Exception{
		System.console().readPassword();
		inputReader=new MyInputReader();
		terminal = new BasicTerminal(inputReader);
		terminal.process();
		System.out.println("输入帐号");
		Scanner sc=new Scanner(System.in);
		String account=sc.next();
		System.out.println("读取到的帐号是 "+account);
		System.out.println("输入密码");
		terminal.disableInputVisibility();
		String password=sc.next();
		terminal.enableInputVisibility();
		System.out.println("读取到的密码是 "+password);
		
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
