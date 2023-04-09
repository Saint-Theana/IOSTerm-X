package io.github.sainttheana;
import java.util.Scanner;

public class TestMain
{

	private static BasicTerminal terminal;

	//private static TestMain.MyInputReader inputReader;




    public static void main(String[] args) throws Exception{
		//inputReader=new MyInputReader();
		terminal = new BasicTerminal();
		terminal.setCursorText("wello term: ");
		terminal.setMaxContentSize(5000);
		terminal.setOverrideStandardErr(true);
		terminal.setOverrideStandardOut(true);
		terminal.setOverrideStandardIn(true);
		terminal.process();
		terminal.out.println("hello world");
		terminal.out.println("hello IOSTerm-X");
		new InputDistributor().start();
		System.out.println("输入帐号");
		InputDistributor.registerHighPriority(new InputDistributor.InputReader(){
				@Override
				public void read(String string)
				{
					System.out.println("读取到的帐号是 "+string);
					System.out.println("输入密码");
					terminal.disableInputVisibility();
					InputDistributor.unRegister(this);
					InputDistributor.registerHighPriority(new InputDistributor.InputReader(){
							@Override
							public void read(String string)
							{
								terminal.enableInputVisibility();
								System.out.println("读取到的密码是 "+string);
							}
						});
				}
		});
		
		int i=0;
		while(true){
			Thread.currentThread().sleep(500);
			System.out.println("\ngyftvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvgyftvhvhv"+i+++"\n");
		}
		
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
