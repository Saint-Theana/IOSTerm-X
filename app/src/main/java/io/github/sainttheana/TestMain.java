package io.github.sainttheana;
import java.util.Scanner;

public class TestMain
{

	private static BasicTerminal terminal;

	//private static TestMain.MyInputReader inputReader;



	private static char esc = 0x1b;
	
    public static void main(String[] args) throws Exception{
		//inputReader=new MyInputReader();
		terminal = new BasicTerminal();
		
		terminal.setCursorText("root@computer " + esc + "[32;m~" + esc + "[0m# ");
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
					InputDistributor.unregister(this);
					InputDistributor.registerHighPriority(new InputDistributor.InputReader(){
							@Override
							public void read(String string)
							{
								terminal.enableInputVisibility();
								System.out.println("读取到的密码是 "+string);
								InputDistributor.unregister(this);
							}
						});
				}
		});
		
		/*new Thread(new Runnable(){

				@Override
				public void run()
				{
					int i=0;
					while (true){
						try
						{
							//Thread.currentThread().sleep(1);
							System.out.println(i++);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					// TODO: Implement this method
				}
				
			
		}).start();*/
		
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
