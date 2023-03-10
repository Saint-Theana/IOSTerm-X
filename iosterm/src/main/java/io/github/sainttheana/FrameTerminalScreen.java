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

	

	@Override
	public Thread newThread(Runnable p1)
	{
		return new Thread(p1, "terminal" + p1.hashCode());
	}

	private ThreadPoolExecutor executor=new ThreadPoolExecutor(10, 30, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1), this, new ThreadPoolExecutor.DiscardOldestPolicy());


	private String empty ="                                                                                                                                                                                                         ";

    private static final int maxFrame = 500;
    private LimitedList<String> contents = new LimitedList<>(maxFrame);//????????????
	private List<String> buffer = new ArrayList<>(maxFrame);//?????????????????????????????????????????????
    private int contentStartIndex = 0;//???????????????????????????
    private int displayStartPosition = 0;//?????????????????????
    private int displayEndPosition = 0;//?????????????????????
    private int displaySize = 0;//?????????????????????,???????????????????????????
    private int currentContentEndPosition = 0;//????????????????????????????????????
	private int bufferSizeOverflow =0;//?????????buffer?????????????????????????????????????????????????????????????????????????????????????????????
    private TextGraphics textGraphics;
	private boolean resizing;

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Action action=printQueue.take();
				long ts=System.currentTimeMillis();
				if(ts-lastResizeTime<200){
					Thread.currentThread().sleep(200);
				}
				switch (action.type)
				{
					case PrintLn:
						internalPrint(action.content+"\n");
						break;
					case Print:
						internalPrint(action.content);
						break;
					case UptldateInput:
						putCSIStyledString(0,getTerminalSize().getRows()-1,action.content);
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
		//should not proceed while resizing
		/*executor.execute(new Runnable(){
				@Override
				public void run()
				{
					*/
					try
					{
						printQueue.put(new Action(ActionType.Print, p0));
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
			/*	}
			});*/



	}


	public void println(final String p0)
	{
		//should not proceed while resizing
//		executor.execute(new Runnable(){
//				@Override
//				public void run()
//				{
					try
					{
						printQueue.put(new Action(ActionType.PrintLn, p0));
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
//				}
//			});
	}



	public void onResize()
	{
		lastResizeTime=System.currentTimeMillis();
		//??????????????????????????????rezise???????????????????????????
		resizing = true;
		//long startTime=System.currentTimeMillis();
		//???????????????????????????????????????????????????
		if (this.textGraphics == null)
		{
			return;
		}
		//setScrollRange(0, getTerminalSize().getRows() - 2);
		this.displayStartPosition = 0;//???????????????????????????
        this.displayEndPosition = getTerminalSize().getRows() - 1;//??????????????????????????????
		displaySize = displayEndPosition - displayStartPosition;

		//long endTime1=System.currentTimeMillis();
		//System.err.println("refreshBuffer took "+(endTime1-startTime)+"ms");
		if (this.displaySize != 0)
		{
			//??????????????????????????????????????????
			if (buffer.size() > displaySize)
			{
				contentStartIndex = 0;
				scrollToBottom();
				//long endTime2=System.currentTimeMillis();
				//System.err.println("scrollToBottom took "+(endTime2-startTime)+"ms");
			}
			else
			{
				//??????????????????????????????
				//????????????????????????
				contentStartIndex = 0;
				currentContentEndPosition = buffer.size();
				refreshFrame();
				//long endTime2=System.currentTimeMillis();
				//System.err.println("refreshFrame took "+(endTime2-startTime)+"ms");
			}
			refreshBuffer();//??????buffer
			refreshFrame();
		}
		//long endTime=System.currentTimeMillis();
		//System.err.println("onResize took "+(endTime-startTime)+"ms");
		resizing = false;
	}


    private String generateEmptyString(int columns)
	{
        return empty.substring(0, columns);
    }


    public void putCSIStyledString(int column, int inputLine, String string) throws RejectedExecutionException
	{
        if (displayStartPosition < inputLine && inputLine < displayEndPosition)
		{
			return;
            //throw new RejectedExecutionException("cannot putCSIStyledString inside scroll range.");
        }
        if (textGraphics != null)
		{
            textGraphics.putCSIStyledString(column, inputLine, string);
        }
		//refreshInternal();
    }

	private void refreshInternal()
	{
		synchronized(this){
			try
			{
				refresh();
			}
			catch (Exception e)
			{
				
				e.printStackTrace();
			}
		}
	}
	
	private String lineBuffer="";

	private void internalPrint(String p0)
	{
		for(int a =0;a<p0.toCharArray().length;a++){
			char t=p0.charAt(a);
			if(t=='\n'){
				if(contents.size()==0){
					addNewLine(lineBuffer);
				}else{
				    setLastLine(lineBuffer);
				}
				addNewLine("");
				lineBuffer="";
			}else{
				lineBuffer+=t;
			}
		}
		setLastLine(lineBuffer);
		refreshBuffer();
		refreshFrame();
		//tryRefresh();
	}

//	private void tryRefresh()
//	{
//		refreshBuffer();
//		refreshFrame();
//	}

	
	private void setLastLine(String string)
	{
		contents.set(contents.size() - 1, string);
	}


    private void addNewLine(String string)
	{
		lineBuffer="";
		//System.err.println(string);
		//System.err.println("addline currentContentEndPosition: "+currentContentEndPosition+" displayEndPosition: "+displayEndPosition + string );
        if (currentContentEndPosition < displayEndPosition)
		{
			//System.err.println(1);
            //?????????????????????????????????
			contents.add(string);
			refreshBuffer();
			//computeSingleLine(string);

//            textGraphics.putCSIStyledString(0, currentContentEndPosition, string);
//            contents.add(string);
//            currentContentEndPosition++;
        }
		else if (currentContentEndPosition == displayEndPosition)
		{
			//System.err.println(2);
			contents.add(string);
			refreshBuffer();
			scrollToBottom();
			//refreshBuffer();
			//refreshFrame();
		}
		else
		{
			//System.err.println("contentStartIndex: "+contentStartIndex+" displaySize: "+displaySize+" contents: "+contents.size());
            if ((contentStartIndex + (displaySize)) < contents.size() + bufferSizeOverflow)
			{
				//System.err.println(3);
                //?????????????????????????????????
                contents.add(string);
            }
			else if ((contentStartIndex + (displaySize)) == contents.size() + bufferSizeOverflow)
			{
				//System.err.println(4);
				contents.add(string);
				refreshBuffer();
				scrollToBottom();
				//refreshBuffer();
				//   refreshFrame();

			}
			else if (buffer.size() == maxFrame)
			{
				//System.err.println(5);
				contents.remove(0);
                contents.add(string);
				refreshBuffer();
				scrollToBottom();
				//refreshBuffer();
				//  refreshFrame();
            }
			else
			{
				//System.err.println(6);
                contents.add(string);
                refreshBuffer();
				scrollToBottom();
				//refreshBuffer();
				//  refreshFrame();

            }
        }
		refreshFrame();
    }

    public void pageUp(int p0)
	{

    }

    public void pageDown(int p0)
	{

    }

    public boolean scrollToBottom()
	{
        if ((contentStartIndex + (displaySize)) > contents.size() + bufferSizeOverflow)
		{
		   // new RejectedExecutionException("contentStartIndex:" + contentStartIndex + "to bottom over scroll the end.").printStackTrace();
            return false;
        }
		else
		{
			while (true)
			{
				if (scrollLines(1) == false)
				{
					break;
				}
			}
			/*
			 contentStartIndex = contents.size() - displaySize;
			 refreshBuffer();
			 refreshFrame();
			 */
            return true;
        }
    }


	public void refreshBuffer()
	{
		long startTime=System.currentTimeMillis();
		buffer.clear();
		int bufferSize=0;
		int contentSize=0;
		currentContentEndPosition = 0;
		for (int i=0;i < contents.size();i++)
		{
			String content=contents.get(i);
			List<String> parseds =new StringContentParser(content, getTerminalSize().getColumns()).parse();
			for (String parsed:parseds)
			{
				//System.err.println("parsed "+parsed);
				buffer.add(parsed);
				bufferSize++;
				//textGraphics.putCSIStyledString(0, currentContentEndPosition, parsed);
				currentContentEndPosition++;
			}
			contentSize++;
		}
		bufferSizeOverflow = bufferSize - contentSize;
		long endTime=System.currentTimeMillis();
		//System.err.println("refreshBuffer took "+(endTime-startTime)+"ms");
	}



    public boolean scrollLines(int p0) throws RejectedExecutionException
	{
		long startTime=System.currentTimeMillis();
        if (buffer.size() < displaySize)
		{
			////System.err.println(buffer.size()+" "+displaySize);
			//  new RejectedExecutionException("cannot scrool when content size less than or equals with scroll range size.").printStackTrace();
            return false;
        }
        if (p0 < 0)
		{
            if ((contentStartIndex + p0) < 0)
			{
				// new RejectedExecutionException("contentStartIndex:" + contentStartIndex + " + " + p0 + " over scroll the top.").printStackTrace();
                return false;
            }
        }
		else
		{//
            if ((contentStartIndex + p0 + (displaySize)) > contents.size() + bufferSizeOverflow)
			{
				// new RejectedExecutionException("contentStartIndex:" + contentStartIndex + " + displaysize: "+displaySize+" + " + p0 + " over scroll the contents end: "+contents.size()+bufferSizeOverflow).printStackTrace();
                return false;
            }
        }
        contentStartIndex += p0;
		//refreshBuffer();
		// refreshFrame();
		long endTime=System.currentTimeMillis();
		//System.err.println("scrollLines took "+(endTime-startTime)+"ms");
        return true;

    }

    public void refreshFrame()
	{
		long startTime=System.currentTimeMillis();
        String a = generateEmptyString(getTerminalSize().getColumns());
        for (int c = displayStartPosition; c < displayEndPosition; c += 1)
		{
            //??????
			////System.err.println("c: "+c);
            textGraphics.putCSIStyledString(0, c, a);
            int locatedIndex = (c - displayStartPosition + contentStartIndex);
            if (buffer.size() > locatedIndex && locatedIndex >= 0)
			{
				////System.err.println(buffer.get(locatedIndex));
                textGraphics.putCSIStyledString(0, c, buffer.get(locatedIndex));
            }
        }
		long endTime=System.currentTimeMillis();
		//System.err.println("refreshFrame took "+(endTime-startTime)+"ms");
    }

    @Override
    public void startScreen() throws IOException
	{
        textGraphics = newTextGraphics();
        super.startScreen();
		onResize();
    }


}
