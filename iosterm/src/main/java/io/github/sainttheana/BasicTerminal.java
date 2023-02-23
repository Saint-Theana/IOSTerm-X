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

public class BasicTerminal implements TerminalResizeListener,Runnable
{

	private TerminalInputStream inputStream;

	@Override
	public void run()
	{
		while(true){
			try
			{
				TerminalSize size=resizeQueue.take();
				//Thread.currentThread().sleep(100);
				//if(System.currentTimeMillis()-lastResizeTime<100){
					//continue;
				//}
			    screen.onResize();
				if (inputer != null)
				{
					inputer.onResize(size);
				}
				
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	
	
	
	private LinkedBlockingQueue<TerminalSize> resizeQueue=new LinkedBlockingQueue<TerminalSize>(1);
	
	
	//private ThreadPoolExecutor executor=new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), this, new ThreadPoolExecutor.AbortPolicy());

	private FrameTerminalScreen screen;
    private int historyIndex = 0;
    private Inputer inputer;
    private LimitedList<String> history = new LimitedList<>(100);
    private boolean running = false;
    private BasicTerminal.InputReader reader;
	private BasicTerminal.InputReader currentReader;
    private boolean scrollMode;
	private boolean inputVisibility=true;
	private long lastResizeTime;
	
	public void disableInputVisibility()
	{
		inputVisibility = false;
		inputer.disableInputVisibility();
	}

	public void enableInputVisibility()
	{
		inputVisibility = true;
		inputer.enableInputVisibility();
	}



    @Override
    public void onResized(Terminal p1, TerminalSize p2)
	{
        //System.err.println(p2);
        screen.doResizeIfNecessary();
        adjustScreenSize(p2);
    }

    public BasicTerminal(InputReader reader) throws Exception
	{
        DefaultTerminalFactory factory =
			new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8"));
        Terminal term = factory.createTerminal();
        screen = new FrameTerminalScreen(term);
		TerminalPrintStream a=new TerminalPrintStream(screen);
		inputStream=new TerminalInputStream();
		System.setOut(a);
		System.setErr(a);
		System.setIn(inputStream);
        this.adjustScreenSize(screen.getTerminalSize());
        this.reader = reader;
		this.currentReader = reader;
        initHistory(new File(".history"));
        term.addResizeListener(this);
		new Thread(this,"resize").start();
    }

    private void adjustScreenSize(final TerminalSize p2)
	{
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

    public void initHistory(File file)
	{
        try
		{
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

	public void interceptReader(BasicTerminal.InputReader reader)
	{
        this.currentReader = reader;
    }

	public void releaseReader()
	{
        this.currentReader = this.reader;
    }

    public interface InputReader
	{
        void read(String input);
    }

    public void setCursorText(String cursorText)
	{
        inputer.setCursorText(cursorText);
    }

    private void executeCommand(String commandBuffer)
	{
        if (currentReader == null)
		{
            return;
        }
		if (inputVisibility)
		{
			saveHistory(new File(".history"));
			//System.err.println(StringEscapeUtils.unescapeEcmaScript(inputer.getCusorText() + commandBuffer));
            System.out.println(StringEscapeUtils.unescapeEcmaScript(inputer.getCusorText() + commandBuffer));
		}
        this.currentReader.read(commandBuffer);
    }

    public void destroy()
	{
        try
		{
            screen.stopScreen();
            running = false;
        }
		catch (IOException e)
		{
            e.printStackTrace();
        }
    }


    private void executeCommand()
	{

        String command = inputer.getInput().trim();
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
			executeCommand(command);
            historyIndex = history.size();
        }
		else
		{
            System.out.println(inputer.getCusorText());
        }
    }


    public void process() throws IOException
	{
        if (running)
		{
            return;
        }
        running = true;
        screen.setTabBehaviour(TabBehaviour.CONVERT_TO_FOUR_SPACES);
        screen.startScreen();
		inputer = new Inputer(screen);
        inputer.updateInput();
        while (true)
		{
            KeyStroke key = screen.readInput();
            if (key != null)
			{
                switch (key.getKeyType())
				{
                    case Character:
						inputStream.write((byte)key.getCharacter().charValue());
                        inputer.append(key.getCharacter());
                        break;
                    case Backspace:
                        // println("Backspace");
						inputStream.deleteLast();
                        inputer.delete();
                        break;
                    case ArrowUp:
                        if (this.scrollMode())
						{
                            screen.scrollLines(-1);
							screen.refreshBuffer();
							screen.refreshFrame();
                        }
						else
						{
                            inputer.clear();
							inputStream.clear();
                            historyIndex = Math.max(0, historyIndex - 1);
                            if (history.size() > 0)
							{
								inputStream.wrap(history.get(historyIndex).getBytes());
                                inputer.wrap(history.get(historyIndex));
                                inputer.gotoEnd();
                            }
							
                        }
                        break;
                    case ArrowDown:
                        if (this.scrollMode())
						{
                            screen.scrollLines(1);
							screen.refreshBuffer();
							screen.refreshFrame();
                        }
						else
						{
                            inputer.clear();
							inputStream.clear();
                            if (historyIndex + 1 == history.size())
							{
                                inputer.wrap("");
                                inputer.gotoEnd();
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
								inputStream.wrap(history.get(historyIndex).getBytes());
                                inputer.wrap(history.get(historyIndex));
                                inputer.gotoEnd();
                            }
                        }
                        break;
                    case ArrowLeft:
						inputer.goLeft();
                        break;
                    case ArrowRight:
                        // println(+terminalBuffer.length() + " " + inputStartIndex + " " +
                        // inputCursor + " " + screen.getTerminalSize().getColumns());
                        inputer.goRight();
                        break;
                    case PageDown:
                        screen.pageDown(1);
                        break;
                    case PageUp:
                        screen.pageUp(1);
                        break;
                    case Tab:
                        break;
                    case Enter:
                        executeCommand();
						inputStream.write((byte)'\n');
						inputer.clear();
                        break;
                    case Escape:
                        if (this.scrollMode())
						{
                            this.leaveScrollMode();
                        }
						else
						{
                            this.enterScrollMode();
                        }
                        break;
                    case Home:
                        inputer.goToStart();
                        break;
                    case End:
                        inputer.gotoEnd();
                        break;
                }
                inputer.updateInput();
            }
        }
    }

    private void enterScrollMode()
	{
        this.scrollMode = true;
    }

    private void leaveScrollMode()
	{
        this.scrollMode = false;
    }

    private boolean scrollMode()
	{
        return this.scrollMode;
    }


}

