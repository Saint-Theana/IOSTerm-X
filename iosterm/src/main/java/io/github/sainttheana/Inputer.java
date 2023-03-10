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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.Collection;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ListIterator;

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

    private String cursorText = "root@computer " + esc + "[32;m~" + esc + "[0m# ";//??????????????????

    private int realCursorLength = StringUtil.getDisplaySizeOfANSIString(cursorText);

	private int inputRow;//??????????????????????????????

	private int sizeOfLine;//???????????????

	private int inputStartIndex;//?????????????????????????????????cursorText?????????

	private int inputContentDisplayStartIndex;//?????????????????????????????????????????????????????????

	private int cursorDisplayStartIndex;

	private volatile List<Character> charList =new ArrayList<Character>();

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
		synchronized(this){
		    updateInputInternal();
		}
	}

	private void updateInputInternal()
	{
		
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
			if (character == 0x1b)//esc ansi?????????????????????????????????????????????
			{
				int length=TerminalTextUtils.getANSIControlSequenceLength(string, i);
				//?????????????????????????????????????????????????????????????????????????????????????????????
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
			else if (TerminalTextUtils.isCharCJK(character))//????????????????????????
			{
				displaySizeOfCharacter = 2;
			}
			else//??????????????????
			{
				displaySizeOfCharacter = 1;
			}

			if (currenLinetSize + displaySizeOfCharacter > sizeOfLine)
			{
				//????????????????????????????????????????????????????????????????????????????????????????????????????????????
				//??????????????????????????????????????????????????????????????????????????????????????????
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
				//???????????????????????????????????????
				currentLine += character;
				currenLinetSize += displaySizeOfCharacter;
			}
		}
		updateCursorPosition();
		clearAllCharacterInInputLine();
		
		if (!inputVisibility)
		{
			displayInputLine(cursorText + currentLine.substring(cursorText.length()).replaceAll(".", "*"));
		}
		else
		{
			displayInputLine(currentLine);
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
		ListIterator<Character> a=clone(charList).listIterator();
		for (;a.hasNext();)
		{
			Character c=a.next();
			if(c==null){
				continue;
			}
			if (offset == 0)
			{
				sb.append(c);
			}
			else if (offset == 1)
			{
				//??????????????????????????????
				//????????????????????????????????????.
				if (TerminalTextUtils.isCharCJK(c))
				{
					sb.append(".");
				}
				offset = 0;
			}
			else if (offset > 1)
			{
				//?????????????????????
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
			//?????????????????????????????????????????????
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
			//???????????????????????????????????????
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
		//???????????????????????????
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
