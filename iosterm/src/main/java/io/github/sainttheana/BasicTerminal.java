/*
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
USA

Please contact Saint-Theana by email the.winter.will.come@gmail.com if you need
additional information or have any questions
*/
package io.github.sainttheana;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.TabBehaviour;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.text.StringEscapeUtils;
import java.util.Queue;
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal;
import com.googlecode.lanterna.input.KeyType;

public class BasicTerminal implements ThreadFactory
{

	@Override
	public Thread newThread(Runnable p1)
	{
		return new Thread(p1,"read");
	}

	private TerminalInputStream inputStream;
	private LinkedBlockingQueue<TerminalSize> resizeQueue=new LinkedBlockingQueue<TerminalSize>(1);
	private FrameTerminalScreen screen;
    private int historyIndex = 0;
    
    private LimitedList<String> history = new LimitedList<>(100);
    private boolean inputVisibility=true;
	private long lastResizeTime;
	private boolean overrideStandardIn=false;
	private boolean overrideStandardErr=false;
	private boolean overrideStandardOut=false;
	public TerminalPrintStream out;
	private boolean browsing=false;
	public void setOverrideStandardOut(boolean overrideStandardOut)
	{
		this.overrideStandardOut = overrideStandardOut;
	}

	public boolean isOverrideStandardOut()
	{
		return overrideStandardOut;
	}

	public void setOverrideStandardErr(boolean overrideStandardErr)
	{
		this.overrideStandardErr = overrideStandardErr;
	}

	public boolean isOverrideStandardErr()
	{
		return overrideStandardErr;
	}

	public void setOverrideStandardIn(boolean overrideStandardIn)
	{
		this.overrideStandardIn = overrideStandardIn;
	}

	public boolean isOverrideStandardIn()
	{
		return overrideStandardIn;
	}
	
	public void disableInputVisibility()
	{
		inputVisibility = false;
		screen.disableInputVisibility();
	}

	public void enableInputVisibility()
	{
		inputVisibility = true;
		screen.enableInputVisibility();
	}

	Runnable resizeThread=new Runnable(){
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					TerminalSize size=resizeQueue.take();
					screen.onResize(size);
					
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	};

	
	//private StringBuilder inputBuffer=new StringBuilder();

	Runnable readThread=new Runnable(){

		
		@Override
		public void run()
		{
			while (true)
			{
				KeyStroke key=null;
				try
				{
					key = screen.readInput();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				if (key != null)
				{
					//System.err.println(key.getKeyType());
					if(key.isCtrlDown()&&key.getKeyType()==KeyType.Character){
						switch (key.getCharacter())
						{
							case 'f':
								screen.freeze();
								break;
							case 'b':
								screen.browse();
								browsing=!browsing;
								break;
						}
						continue;
					}
					
					switch (key.getKeyType())
					{
						case Character:
							//inputBuffer.append(key.getCharacter());
							screen.append(key.getCharacter());
							break;
						case Backspace:
							// println("Backspace");
							//inputBuffer.deleteCharAt(inputBuffer.length()-1);
							screen.delete();
							break;
						case ArrowUp:
							if (screen.isBrowsing())
							{
								screen.scrollLines(-1);
								
							}
							else
							{
								//inputBuffer.delete(0,inputBuffer.length()-1);
								screen.clear();
								historyIndex = Math.max(0, historyIndex - 1);
								if (history.size() > 0)
								{
									//inputBuffer.append(history.get(historyIndex).getBytes());
									screen.wrap(history.get(historyIndex));
									screen.gotoEnd();
								}
							}
							break;
						case ArrowDown:
							if (screen.isBrowsing())
							{
								screen.scrollLines(1);
							}
							else
							{
								//inputStream.clear();
								screen.clear();
								if (historyIndex + 1 == history.size())
								{
									screen.wrap("");
									screen.gotoEnd();
									historyIndex = history.size();
									break;
								}
								else if (historyIndex + 1 > history.size())
								{
									break;
								}
								// println(history.size()+" "+historyIndex);
								historyIndex = Math.max(0, Math.min(history.size() - 1, historyIndex + 1));
								if (history.size() > 0)
								{
									//inputStream.wrap(history.get(historyIndex).getBytes());
									screen.wrap(history.get(historyIndex));
									screen.gotoEnd();
								}
							}
							break;
						case ArrowLeft:
							screen.goLeft();
							break;
						case ArrowRight:
							// println(+terminalBuffer.length() + " " + inputStartIndex + " " +
							// inputCursor + " " + screen.getTerminalSize().getColumns());
							screen.goRight();
							break;
						case PageDown:
							if(browsing){
							    screen.pageDown(1);
							}
							break;
						case PageUp:
							if(browsing){
							    screen.pageUp(1);
							}
							break;
						case Tab:
							break;
						case Enter:
							//inputStream.write((byte)'\n');
							executeCommand();
							screen.clear();
							break;
						case Escape:
							if (screen.isFrozen())
							{
								screen.freeze();
							}
							else if(screen.isBrowsing())
							{
								screen.browse();
							}
							break;
						case Home:
							screen.goToStart();
							break;
						case End:
							screen.gotoEnd();
							break;
					}
					screen.updateInput();
				}
			}
		}
	};

	private class MyTerminalResizeListener implements TerminalResizeListener
	{
		@Override
		public void onResized(Terminal p1, TerminalSize p2)
		{
			//System.err.println(p2);
			
			adjustScreenSize(p2);
			screen.doResizeIfNecessary();
		}
	}
	
	public void setMaxContentSize(int size){
	    screen.setMaxContentSize(size);
	}

    public BasicTerminal(/*InputReader reader*/) throws Exception
	{
        DefaultTerminalFactory factory =
			new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"));
		//factory.setUnixTerminalCtrlCBehaviour(UnixLikeTerminal.CtrlCBehaviour.TRAP);
        Terminal term = factory.createTerminal();
		
        screen = new FrameTerminalScreen(term);
		//inputer = new Inputer(screen);
        this.adjustScreenSize(screen.getTerminalSize());
       // this.reader = reader;
		//this.currentReader = reader;
        initHistory(new File(".history"));
        term.addResizeListener(screen);
        //term.addResizeListener(inputer);
		new Thread(resizeThread,"Virtual-Resize-Thread").start();
    }

    private void adjustScreenSize(final TerminalSize p2)
	{
	   // screen.onResize();
	    
	}/*
		lastResizeTime = System.currentTimeMillis();
		try
		{
			resizeQueue.put(p2);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
    }
    */

    public void initHistory(File file)
	{
        try
		{
			if(!file.exists()){
				file.createNewFile();
			}
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            while (line != null)
			{
                history.add(line);
                line = br.readLine();
            }
            historyIndex = history.size();
        }
		catch (Exception e)
		{
            e.printStackTrace();
        }
    }

    public void saveHistory(File file)
	{
        try
		{
            FileWriter fw = new FileWriter(file);
            for (int i = 0; i < history.size(); i += 1)
			{
                fw.write(history.get(i) + "\n");
            }
            fw.flush();
            fw.close();
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }
    }

    public int getColumn()
	{
        return screen.getTerminalSize().getColumns();
    }

	/*public void interceptReader(BasicTerminal.InputReader reader)
	{
        this.currentReader = reader;
    }

	public void releaseReader()
	{
        this.currentReader = this.reader;
    }
	*/

//    public interface InputReader
//	{
//        void read(String input);
//    }

    public void setCursorText(String cursorText)
	{
        screen.setCursorText(cursorText);
    }

    private void executeCommand(final String commandBuffer)
	{

		if (inputVisibility)
		{
			saveHistory(new File(".history"));
			//System.err.println(StringEscapeUtils.unescapeEcmaScript(inputer.getCusorText() + commandBuffer));
            System.out.print(StringEscapeUtils.unescapeEcmaScript(screen.getCusorText() + commandBuffer));
		}
		inputStream.wrap(commandBuffer.getBytes());
    }

    public void destroy()
	{
        try
		{
            screen.stopScreen();
            //running = false;
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }
    }


    private void executeCommand()
	{
        String command = screen.getInput().trim();
        if (command.length() > 0)
		{
            // println(command);
			if (inputVisibility)
			{
                history.add(command);
			}
			else
			{
				history.add(command.replaceAll(".", "*"));
			}
			executeCommand(command+"\n");
            historyIndex = history.size();
        }
		else
		{
            screen.println(screen.getCusorText());
        }
    }

	

    public void process() throws IOException
	{
		out=new TerminalPrintStream(screen);
		inputStream = new TerminalInputStream();
		if(overrideStandardIn){
			System.setIn(inputStream);
		}
		if(overrideStandardOut){
		    System.setOut(out);
		}
		if(overrideStandardErr){
		    System.setErr(out);
		}
        screen.setTabBehaviour(TabBehaviour.CONVERT_TO_FOUR_SPACES);
        screen.startScreen();
        screen.updateInput();
        new Thread(readThread,"Virtual-Read-Thread").start();
    }



}

