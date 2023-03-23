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

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FrameTerminalScreen extends TerminalScreen implements Runnable,ThreadFactory
{
	private long lastResizeTime;

	private boolean refreshingFrame;

	private boolean frozen=false;

	private boolean browsing =false;

	public boolean isBrowsing()
	{
		return browsing;
	}

	public boolean isFrozen()
	{
		return frozen;
	}
	
	public void browse()
	{
		browsing= !browsing;
		if(!browsing){
			refreshBuffer();
			refreshFrame();
		}
	}

	public void freeze()
	{
		frozen = !frozen;
		if(!frozen){
			refreshBuffer();
			refreshFrame();
		}
	}


	@Override
	public Thread newThread(Runnable p1)
	{
		return new Thread(p1, "terminal" + p1.hashCode());
	}

	private ThreadPoolExecutor executor=new ThreadPoolExecutor(10, 30, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1), this, new ThreadPoolExecutor.DiscardOldestPolicy());

	private String empty ="                                                                                                                                                                                                         ";

//    private static final int maxFrame = 500;
//    private LimitedList<String> contents = new LimitedList<>(maxFrame);//原始数
	private List<String> buffer = new ArrayList<>();//缓存每一行分行的数据，用于显示
//    private int contentStartIndex = 0;//数据开始显示的位置
    private int displayStartPosition = 0;//滚动区开始位置
    private int displayEndPosition = 0;//滚动区结束位置
    private int displaySize = 0;//活动区的总行数,也就是显示区的行数
//    private int currentContentEndPosition = 0;//滚动区数据最后一行的位置
//	private int bufferSizeOverflow =0;//当前的buffer可能由于超长字符串拆分为多行，从而造成比原始数据行数更多的情况
    private TextGraphics textGraphics;
	private Text text;
	private boolean resizing;



	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Action action=printQueue.take();
//				long ts=System.currentTimeMillis();
//				if (ts - lastResizeTime < 500)
//				{
//					Thread.currentThread().sleep(500);
//				}
				switch (action.type)
				{
					case PrintLn:
						internalPrint(action.content + "\n");
						break;
					case Print:
						internalPrint(action.content);
						break;
					case UptldateInput:
						putCSIStyledString(0, getTerminalSize().getRows() - 1, action.content);
						break;
				}
				refreshInternal();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private LinkedBlockingQueue<Action> printQueue=new LinkedBlockingQueue<Action>(100);

	private class Action
	{
		public ActionType type;

		public String content;

		public Action(ActionType type, String content)
		{
			this.type = type;
			this.content = content;
		}
	}

	private enum ActionType
	{
		PrintLn,
		Print,
		UptldateInput;
	}

    public FrameTerminalScreen(Terminal term) throws IOException
	{
        super(term);
		new Thread(this, "print").start();
		text = new Text();
		//System.setOut();
    }

	public void updateInput(String string)
	{
		try
		{
			printQueue.put(new Action(ActionType.UptldateInput, string));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void print(final String p0)
	{

		try
		{
			printQueue.put(new Action(ActionType.Print, p0));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}


	public void println(final String p0)
	{
		try
		{
			printQueue.put(new Action(ActionType.PrintLn, p0));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}



	public void onResize()
	{
		lastResizeTime = System.currentTimeMillis();
		//可能在短时间内有很多rezise调用，所以延迟执行
		resizing = true;
		//long startTime=System.currentTimeMillis();
		//重新设置大小后不管他直接拉到最底下
		if (this.textGraphics == null)
		{
			return;
		}
		//setScrollRange(0, getTerminalSize().getRows() - 2);
		this.displayStartPosition = 0;//开始位置总在第一行
        this.displayEndPosition = getTerminalSize().getRows() - 1;//结束位置在倒数第二行
		displaySize = displayEndPosition - displayStartPosition;
		resizing = false;
		if(frozen){
			scrollLines(0);
		}else if(browsing){
			scrollLines(0);
		}else{
			refreshBuffer();
			refreshFrame();
		}
	}


    private String generateEmptyString(int columns)
	{
        return empty.substring(0, columns);
    }


    public void putCSIStyledString(int column, int inputLine, String string) throws RejectedExecutionException
	{
//        if (displayStartPosition < inputLine && inputLine < displayEndPosition)
//		{
//			return;
//            //throw new RejectedExecutionException("cannot putCSIStyledString inside scroll range.");
//        }
        if (textGraphics != null)
		{
            textGraphics.putCSIStyledString(column, inputLine, string);
        }
		//refreshInternal();
    }

	private void refreshInternal()
	{
//		synchronized (this)
//		{
			try
			{
				refresh();
			}
			catch (Exception e)
			{

				e.printStackTrace();
			}
		//}
	}

	private String lineBuffer="";

	private void internalPrint(String p0)
	{
		for (int a =0;a < p0.toCharArray().length;a++)
		{
			char t=p0.charAt(a);
			if (t == '\n')
			{
				if (text.getSize() == 0)
				{
					addNewLine(lineBuffer);
				}
				else
				{
				    setLastLine(lineBuffer);
				}
				addNewLine("");
				lineBuffer = "";
			}
			else
			{
				lineBuffer += t;
			}
		}
	}


	private void setLastLine(String string)
	{
		text.setLast(string);
	}


    private void addNewLine(String string)
	{
		text.appendLine(string);
		if (frozen||browsing)
		{
			return;
		}
		refreshBuffer();
		refreshFrame();
    }

    public void pageUp(int p0)
	{
		
    }

    public void pageDown(int p0)
	{
		
    }

	public void refreshBuffer()
	{
		if (refreshingFrame)
		{
			return;
		}
		long startTime=System.currentTimeMillis();
		buffer.clear();
		List<String> lastLines=text.getLastLines(displaySize);
		//currentContentEndPosition = 0;
		for (int i=0;i < lastLines.size();i++)
		{
			String content=lastLines.get(i);
			List<String> parseds =new StringContentParser(content, getTerminalSize().getColumns()).parse();
			for (String parsed:parseds)
			{
				//System.err.println("parsed "+parsed);
				buffer.add(parsed);
				//textGraphics.putCSIStyledString(0, currentContentEndPosition, parsed);
				//currentContentEndPosition++;
			}
		}
		//最后一行是空的就把最后一行去掉
		if(buffer.size()>0&&buffer.get(buffer.size()-1).isEmpty()){
			buffer.remove(buffer.size()-1);
		}
		//把开头超出的去掉确保最低下的能显示
		while (buffer.size() > displaySize)
		{
			buffer.remove(0);
		}
		//bufferSizeOverflow = bufferSize - contentSize;
		long endTime=System.currentTimeMillis();
		//System.err.println("refreshBuffer took "+(endTime-startTime)+"ms");
	}



    public boolean scrollLines(int p0) throws RejectedExecutionException
	{
		if(!browsing||frozen){
			throw new IllegalStateException("terminal is not browsing or is frozen");
		}
		long startTime=System.currentTimeMillis();
		buffer.clear();
		List<String> lastLines=text.getLines(Math.max(0,text.getCurrentIndex()+p0),displaySize);
		//currentContentEndPosition = 0;
		for (int i=0;i < lastLines.size();i++)
		{
			String content=lastLines.get(i);
			List<String> parseds =new StringContentParser(content, getTerminalSize().getColumns()).parse();
			for (String parsed:parseds)
			{
				//System.err.println("parsed "+parsed);
				buffer.add(parsed);
				//textGraphics.putCSIStyledString(0, currentContentEndPosition, parsed);
				//currentContentEndPosition++;
			}
		}
		
		//bufferSizeOverflow = bufferSize - contentSize;
		long endTime=System.currentTimeMillis();
		refreshFrame();
		//System.err.println("refreshBuffer took "+(endTime-startTime)+"ms");
		return true;
    }

    public void refreshFrame()
	{
		refreshingFrame = true;
		long startTime=System.currentTimeMillis();
        String a = generateEmptyString(getTerminalSize().getColumns());
		int contentIndex=0;
		for (int c = displayStartPosition; c < displayEndPosition; c += 1)
		{
            textGraphics.putCSIStyledString(0, c, a);
        }
        for (int c = displayStartPosition; c < displayEndPosition; c += 1)
		{
            
            if (contentIndex < buffer.size())
			{
				////System.err.println(buffer.get(locatedIndex));
                textGraphics.putCSIStyledString(0, c, buffer.get(contentIndex));
            }
			contentIndex++;
        }
		long endTime=System.currentTimeMillis();
		//System.err.println("refreshFrame took "+(endTime-startTime)+"ms");
		refreshingFrame = false;
    }

    @Override
    public void startScreen() throws IOException
	{
        textGraphics = newTextGraphics();
        super.startScreen();
		onResize();
    }


}
