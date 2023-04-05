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

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

public class FrameTerminalScreen extends TerminalScreen implements Runnable,ThreadFactory,TerminalResizeListener
{
	private long lastResizeTime;

	private boolean refreshingFrame;

	private boolean frozen=false;

	private boolean browsing =false;

	private String empty ="                                                                                                                                                                                                         ";

	private char esc = 0x1b;

    private String cursorText = "root@computer " + esc + "[32;m~" + esc + "[0m# ";//每一行的开头

    private int realCursorLength = StringUtil.getDisplaySizeOfANSIString(cursorText);

	private int inputRow;//确定在第几行显示输入

	private int sizeOfLine;//一行的宽度

	private int inputStartIndex;//输入开始的位置，要减去cursorText的长度

	private int inputContentDisplayStartIndex;//输入内容显示开始的位置，有可能不是开头

	private int cursorDisplayStartIndex;

	private volatile List<Character> charList =new ArrayList<Character>();

	private int currentIndexOfInput;

	private int currentCursorIndexOfTerminal;

	private boolean inputVisibility=true;

    private String displayInputLine="";

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
		browsing = !browsing;
		if (!browsing)
		{
			refreshBuffer();
			refreshFrame();
		}
	}

	public void freeze()
	{
		frozen = !frozen;
		if (!frozen)
		{
			refreshBuffer();
			refreshFrame();
		}
	}

    @Override
	public void onResized(Terminal p1, TerminalSize p2)
	{
		//System.err.println(p2);
		doResizeIfNecessary();
		onResize(p2);

	}

	@Override
	public Thread newThread(Runnable p1)
	{
		return new Thread(p1, "terminal" + p1.hashCode());
	}


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
				switch (action.type)
				{
					case PrintLn:
						internalPrint(action.content + "\n");
						refreshBuffer();
						break;
					case Print:
						internalPrint(action.content);
						refreshBuffer();
						break;
					case UptldateInput:
						//putCSIStyledString(0, getTerminalSize().getRows() - 1, action.content);
						break;
				}
				refreshFrame();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		}
	}

	private LinkedBlockingQueue<Action> printQueue=new LinkedBlockingQueue<Action>(1000);

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
        text = new Text();
		new Thread(this, "Virtual-Print-Thread").start();

		//System.setOut();
    }

	public void updateInput(String string)
	{
		/*  putCSIStyledString(0, getTerminalSize().getRows() - 1, string);
		 refreshFrame();
		 */
		//printQueue.offer(new Action(ActionType.UptldateInput, string));
		// Thread.ofVirtual().name("Virtual-Action-Thread").start(new Runnable(){
		//        	@Override
		//       	public void run()
		//       	{
		try
		{
			printQueue.put(new Action(ActionType.UptldateInput, string));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}


		//        	}

		//	    });
	    /*

		 try
		 {
		 printQueue.put(new Action(ActionType.UptldateInput, string));
		 }
		 catch (InterruptedException e)
		 {
		 e.printStackTrace();
		 }
		 */

	}

	public void print(final String p0)
	{
		/*  internalPrint(p0);
		 refreshBuffer();
		 refreshFrame();
		 */
	    //printQueue.offer(new Action(ActionType.Print, p0));

		try
		{
			printQueue.put(new Action(ActionType.Print, p0));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}


		/*try
		 {
		 printQueue.put(new Action(ActionType.Print, p0));
		 }
		 catch (InterruptedException e)
		 {
		 e.printStackTrace();
		 }
		 */
	}


	public void println(final String p0)
	{
	    /*internalPrint(p0+ "\n");
		 refreshBuffer();
		 refreshFrame();
		 */
	    //printQueue.offer(new Action(ActionType.PrintLn, p0));

		try
		{
			printQueue.put(new Action(ActionType.PrintLn, p0));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}


		/*try
		 {
		 printQueue.put(new Action(ActionType.PrintLn, p0));
		 }
		 catch (InterruptedException e)
		 {
		 e.printStackTrace();
		 }
		 */
	}


	public void initSize()
	{
		//setScrollRange(0, getTerminalSize().getRows() - 2);
		this.displayStartPosition = 0;//开始位置总在第一行
        this.displayEndPosition = getTerminalSize().getRows() - 1;//结束位置在倒数第二行
		displaySize = displayEndPosition - displayStartPosition;
		refreshBuffer();
		refreshFrame();
	}


	public void onResize(TerminalSize p2)
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
        this.displayEndPosition = p2.getRows() - 1;//结束位置在倒数第二行
		displaySize = displayEndPosition - displayStartPosition;
		inputRow = p2.getRows() - 1;
		sizeOfLine = p2.getColumns();

		resizing = false;

		if (frozen)
		{
			scrollLines(0);
		}
		else if (browsing)
		{
			scrollLines(0);
		}
		else
		{
			refreshBuffer();
			refreshFrame();
		}




		setCursorPosition(new TerminalPosition(realCursorLength + cursorDisplayStartIndex, inputRow));
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
		//synchronized (this)
		//{
		if (System.currentTimeMillis() - lastResizeTime < 200)
		{
		    new Thread(new Runnable(){
		        	@Override
		        	public void run()
		        	{
		        	    try
		        	   	{
		        	   		Thread.sleep(200);
	        	   		}
		        	   	catch (Exception e)
	        	   		{
	        	   		    e.printStackTrace();
	        	   		}
		        	    refreshInternal();

		        	}

				}, "Virtual-Print-Thread").start();
	    	return;
		}
		try
		{
			refresh();
		}
		catch (Exception e)
		{

			e.printStackTrace();
		}
		//	}
	}

	private void internalPrint(String p0)
	{
	    String lastLine=text.getLastLine();
		for (int a =0;a < p0.toCharArray().length;a++)
		{
			char t=p0.charAt(a);
			if (t == '\n')
			{
				if (text.getSize() == 0)
				{
					addNewLine(lastLine);
				}
				else
				{
				    setLastLine(lastLine);
				}
				addNewLine("");
				lastLine = "";
			}
			else
			{
				lastLine += t;
			}
		}
		if (text.getSize() == 0)
		{
			addNewLine(lastLine);
		}
		else
		{
			setLastLine(lastLine);
		}
	}


	private void setLastLine(String string)
	{
		text.setLast(string);
	}


    private void addNewLine(String string)
	{
		text.appendLine(string);
		if (frozen || browsing)
		{
			return;
		}
    }

    public void pageUp(int p0)
	{

    }

    public void pageDown(int p0)
	{

    }

	public void refreshBuffer()
	{
		
		long startTime=System.currentTimeMillis();
		buffer.clear();
		List<String> lastLines=text.getLastLines(displaySize);
		//System.err.println(lastLines);
		//currentContentEndPosition = 0;
		for (int i=0;i < lastLines.size();i++)
		{
			String content=lastLines.get(i);
			List<String> parseds =new StringContentParser(content, getTerminalSize().getColumns()).parse();
			for (String parsed:parseds)
			{
				//	System.err.println("parsed "+parsed);
				buffer.add(parsed);
				//textGraphics.putCSIStyledString(0, currentContentEndPosition, parsed);
				//currentContentEndPosition++;
			}
		}
		//最后一行是空的就把最后一行去掉
		if (buffer.size() > 0 && buffer.get(buffer.size() - 1).isEmpty())
		{
			buffer.remove(buffer.size() - 1);
		}
		//把开头超出的去掉确保最低下的能显示
		while (buffer.size() > displaySize)
		{
			buffer.remove(0);
		}
		//	System.err.println(buffer);
		//bufferSizeOverflow = bufferSize - contentSize;
		long endTime=System.currentTimeMillis();
		//System.err.println("refreshBuffer took "+(endTime-startTime)+"ms");
	}



    public boolean scrollLines(int p0) throws RejectedExecutionException
	{
		if (!browsing || frozen)
		{
			throw new IllegalStateException("terminal is not browsing or is frozen");
		}
		long startTime=System.currentTimeMillis();
		buffer.clear();
		List<String> lastLines=text.getLines(Math.max(0, text.getCurrentIndex() + p0), displaySize);
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
		//new RuntimeException().printStackTrace();
		refreshingFrame = true;
		long startTime=System.currentTimeMillis();
        String a = generateEmptyString(getTerminalSize().getColumns());
		int contentIndex=0;
		for (int c = displayStartPosition; c < displayEndPosition + 1; c += 1)
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
		if (!inputVisibility)
		{
            textGraphics.putCSIStyledString(0, getTerminalSize().getRows() - 1, cursorText + displayInputLine.replaceAll(".", "*"));
		}
		else
		{
			textGraphics.putCSIStyledString(0, getTerminalSize().getRows() - 1, cursorText + displayInputLine);
		}
		long endTime=System.currentTimeMillis();
		//
		
	  //  System.err.println("refreshFrame took "+(endTime-startTime)+"ms");
		refreshingFrame = false;
		refreshInternal();
    }

    @Override
    public void startScreen() throws IOException
	{
        textGraphics = newTextGraphics();
        super.startScreen();
		initSize();
		inputRow = getTerminalSize().getRows() - 1;
		this.sizeOfLine = getTerminalSize().getColumns();
		setCursorPosition(new TerminalPosition(realCursorLength, inputRow));
		refreshFrame();
    }





	public void disableInputVisibility()
	{
		inputVisibility = false;
	}

	public void enableInputVisibility()
	{
		inputVisibility = true;
	}

	public void updateInput()
	{
		//synchronized(this){
		updateInputInternal();
		//}
	}

	private void updateInputInternal()
	{
		String currentLine="";
		String string= getDisplay();
		char[] array=string.toCharArray();
		int currenLinetSize=0;
		for (int i=0;i < array.length;i++)
		{
			//System.err.println("i "+i);
			char character=array[i];
			int displaySizeOfCharacter=0;
			//System.err.println(character);
			if (character == 0x1b)//esc ansi控制字符，这玩意不显示在终端里
			{
				int length=TerminalTextUtils.getANSIControlSequenceLength(string, i);
				//把这个字符拼进去然后还要把对应长度的控制字符写进去并且忽略长度
				//System.err.println(length);
				//currentLine+=character;
				int end=i + length;
				for (int i1=i;i1 < end;i1++)
				{
					//System.err.println("i1 " + i1);
					displayInputLine += array[i1];
				}
				i += length - 1;
				continue;
			}
			else if (TerminalTextUtils.isCharCJK(character))//占用两个字符宽度
			{
				displaySizeOfCharacter = 2;
			}
			else//一个字符宽度
			{
				displaySizeOfCharacter = 1;
			}

			if (currenLinetSize + displaySizeOfCharacter > sizeOfLine)
			{
				//如果把当前的字符拼进去那么会超出长度，理论上当前的是占用两个宽度才会出现
				//所以先把当前的字符串写入，然后当前字符当做下一个字符串的开头
				updateCursorPosition();

				displayInputLine = currentLine;
				refreshFrame();
				return;
			}
			if (currenLinetSize + displaySizeOfCharacter == sizeOfLine)
			{
				currentLine += character;
				updateCursorPosition();
				displayInputLine = currentLine;
				refreshFrame();
				return;
			}
			else
			{
				//还没到一行的宽度所以不写入
				currentLine += character;
				currenLinetSize += displaySizeOfCharacter;
			}
		}
		updateCursorPosition();

		displayInputLine = currentLine;
		refreshFrame();


	}

	private void updateCursorPosition()
	{
		setCursorPosition(new TerminalPosition(realCursorLength + cursorDisplayStartIndex, inputRow));
	}


	private String getDisplay()
	{
		StringBuilder sb=new StringBuilder();
		int offset=inputContentDisplayStartIndex;
		ListIterator<Character> a=clone(charList).listIterator();
		for (;a.hasNext();)
		{
			Character c=a.next();
			if (c == null)
			{
				continue;
			}
			if (offset == 0)
			{
				sb.append(c);
			}
			else if (offset == 1)
			{
				//说明只需要去掉这一个
				//如果这个是中文那就替换成.
				if (TerminalTextUtils.isCharCJK(c))
				{
					sb.append(".");
				}
				offset = 0;
			}
			else if (offset > 1)
			{
				//至少要去掉两个
				if (TerminalTextUtils.isCharCJK(c))
				{
					offset -= 2;
				}
				else
				{
					offset -= 1;
				}
			}

		}
		return sb.toString();
	}

	private List<Character> clone(List<Character> charList)
	{
		List<Character> chars=new ArrayList<Character>();
		chars.addAll(charList);
		return chars;
	}



	public String getCusorText()
	{
		return cursorText;
	}

	public void append(Character character)
	{
		charList.add(currentIndexOfInput, character);
		currentIndexOfInput++;
		int sizeOfCharacter=0;
		if (TerminalTextUtils.isCharCJK(character))
		{
			sizeOfCharacter += 2;
		}
		else
		{
			sizeOfCharacter++;
		}
		if (cursorDisplayStartIndex + realCursorLength >= sizeOfLine)
		{
			//已经到了最右边了，把输入往左顶
			inputContentDisplayStartIndex += sizeOfCharacter;
		}
		else
		{
			cursorDisplayStartIndex += sizeOfCharacter;
		}
		updateInput();
	}

	public void delete()
	{
		if (charList.isEmpty())
		{
			return;
		}
		Character character=charList.remove(currentIndexOfInput - 1);
		currentIndexOfInput--;
		int sizeOfCharacter=0;
		if (TerminalTextUtils.isCharCJK(character))
		{
			sizeOfCharacter += 2;
		}
		else
		{
			sizeOfCharacter++;
		}
		if (inputContentDisplayStartIndex > 0)
		{
			//不是在最左边，把输入往右顶
			inputContentDisplayStartIndex -= sizeOfCharacter;
		}
		else
		{
			cursorDisplayStartIndex = Math.max(cursorDisplayStartIndex - sizeOfCharacter, 0);
		}
		updateInput();
	}

	public void goToStart()
	{
		this.currentCursorIndexOfTerminal = 0;
		this.currentIndexOfInput = 0;
		this.cursorDisplayStartIndex = 0;
		this.inputContentDisplayStartIndex = 0;
		updateInput();
	}

	public void goRight()
	{
		if (charList.size() == currentIndexOfInput)
		{
			return;
		}
		char character=charList.get(currentIndexOfInput);
		currentIndexOfInput++;
		int sizeOfCharacter=0;
		if (TerminalTextUtils.isCharCJK(character))
		{
			sizeOfCharacter += 2;
		}
		else
		{
			sizeOfCharacter++;
		}
		if (cursorDisplayStartIndex + realCursorLength >= sizeOfLine)
		{

			inputContentDisplayStartIndex += sizeOfCharacter;
		}
		else
		{
			cursorDisplayStartIndex += sizeOfCharacter;
		}
	}

	public void goLeft()
	{
		if (currentIndexOfInput == 0)
		{
			return;
		}
		char character=charList.get(currentIndexOfInput - 1);
		currentIndexOfInput--;
		int sizeOfCharacter=0;
		if (TerminalTextUtils.isCharCJK(character))
		{
			sizeOfCharacter += 2;
		}
		else
		{
			sizeOfCharacter++;
		}
		if (inputContentDisplayStartIndex > 0)
		{
			inputContentDisplayStartIndex -= sizeOfCharacter;
		}
		else
		{
			cursorDisplayStartIndex -= sizeOfCharacter;
		}

	}

	public void wrap(String string)
	{
		clear();
		for (char c:string.toCharArray())
		{
			charList.add(c);
		}
		gotoEnd();
	}

	public void gotoEnd()
	{
		while (currentIndexOfInput != charList.size())
		{
			goRight();
		}
	}



	public void setCursorText(String cursorText)
	{
		this.cursorText = cursorText;
	}

	public void clear()
	{
		//超出长度不会有问题
		this.charList.clear();
		this.currentCursorIndexOfTerminal = 0;
		this.currentIndexOfInput = 0;
		this.cursorDisplayStartIndex = 0;
		this.inputContentDisplayStartIndex = 0;
		updateInput();
	}


	public String getInput()
	{
		StringBuilder sb=new StringBuilder();
		for (char c:charList)
		{
			sb.append(c);
		}
		return sb.toString();
	}

}
