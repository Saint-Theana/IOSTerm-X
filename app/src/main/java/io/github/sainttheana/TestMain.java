package io.github.sainttheana;
import java.util.Scanner;

public class TestMain
{

	private static BasicTerminal terminal;

	//private static TestMain.MyInputReader inputReader;




    public static void main(String[] args) throws Exception{
		//inputReader=new MyInputReader();
		terminal = new BasicTerminal();
		terminal.setOverrideStandardErr(false);
		terminal.setOverrideStandardOut(true);
		terminal.setOverrideStandardIn(true);
		terminal.process();
		terminal.out.println("hello world");
		terminal.out.println("hello IOSTerm-X");
		System.out.println("输入帐号");
		Scanner sc=new Scanner(System.in);
		String account=sc.nextLine();
		System.out.println("读取到的帐号是 "+account);
		System.out.println("输入密码");
		terminal.disableInputVisibility();
		String password=sc.nextLine();
		terminal.enableInputVisibility();
		System.out.println("读取到的密码是 "+password);
		int i=0;
		/*while(true){
			Thread.currentThread().sleep(1000);
			System.out.println("\ngyftvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvhv"+i+++"\n");
		}
		*/
    }
	
	
	
//	private static class MyInputReader implements BasicTerminal.InputReader
//	{
//		@Override
//		public void read(String input)
//		{
//			//System.out.println("read input!"+input);
//			
//		}
//	}


}
