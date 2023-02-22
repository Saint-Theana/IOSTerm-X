package io.github.sainttheana;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TerminalTextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Inputer implements ThreadFactory
{

	@Override
	public Thread newThread(Runnable p1)
	{
		return new Thread(p1, "terminal" + p1.hashCode());
	}

	private ThreadPoolExecutor executor=new ThreadPoolExecutor(10, 30, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), this, new ThreadPoolExecutor.DiscardOldestPolicy());



	private String empty ="                                                                                                                                                                                                         ";


	private FrameTerminalScreen screen;

	private char esc = 0x1b;

    private String cursorText = "root@computer " + esc + "[32;m~" + esc + "[0m# ";//每一行的开头

    private int realCursorLength = StringUtil.getDisplaySizeOfANSIString(cursorText);

	private int inputRow;//确定在第几行显示输入

	private int sizeOfLine;//一行的宽度

	private int inputStartIndex;//输入开始的位置，要减去cursorText的长度

	private int inputContentDisplayStartIndex;//输入内容显示开始的位置，有可能不是开头

	private int cursorDisplayStartIndex;

	private List<Character> charList =new ArrayList<Character>();

	private int currentIndexOfInput;

	private int currentCursorIndexOfTerminal;

	private boolean inputVisibility=true;

	public Inputer(FrameTerminalScreen screen)
	{
		this.screen = screen;
		inputRow = screen.getTerminalSize().getRows() - 1;
		this.sizeOfLine = screen.getTerminalSize().getColumns();
		screen.setCursorPosition(new TerminalPosition(realCursorLength, inputRow));
		updateInput();
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
		executor.execute(new Runnable(){
				@Override
				public void run()
				{
					updateInputInternal();
				}
			});
	}

	private void updateInputInternal()
	{
		synchronized (this)
		{
			clearAllCharacterInInputLine();
			String string= cursorText + getDisplay();
			char[] array=string.toCharArray();
			String currentLine="";
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
						currentLine += array[i1];
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
					displayInputLine(currentLine);
					return;
				}
				if (currenLinetSize + displaySizeOfCharacter == sizeOfLine)
				{
					currentLine += character;
					updateCursorPosition();
					displayInputLine(currentLine);
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
			if (!inputVisibility)
			{
				displayInputLine(cursorText + currentLine.substring(cursorText.length()).replaceAll(".", "*"));
			}
			else
			{
				displayInputLine(currentLine);
			}
		}
	}

	private void updateCursorPosition()
	{
		screen.setCursorPosition(new TerminalPosition(realCursorLength + cursorDisplayStartIndex, inputRow));
	}



	private String getDisplay()
	{
		StringBuilder sb=new StringBuilder();
		int offset=inputContentDisplayStartIndex;
		for (char c:charList)
		{
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

	private void clearAllCharacterInInputLine()
	{
		StringBuffer emptyStringBuilder = new StringBuffer();
        for (int i = 0; i < screen.getTerminalSize().getColumns(); i += 1)
		{
            emptyStringBuilder.append(" ");
        }
        screen.updateInput(cursorText + empty.substring(0, screen.getTerminalSize().getColumns() + 1));
	}

	private void displayInputLine(String string)
	{
		//System.err.println(inputRow+"  "+string);
		screen.updateInput(string);

	}

	public void onResize(TerminalSize p2)
	{
		//System.err.println("onResize");
		inputRow = p2.getRows() - 1;
		sizeOfLine = p2.getColumns();
		screen.setCursorPosition(new TerminalPosition(realCursorLength, inputRow));
		updateInput();

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
        clearAllCharacterInInputLine();
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
