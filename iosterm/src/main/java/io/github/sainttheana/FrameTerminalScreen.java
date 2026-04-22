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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.io.*;

public class FrameTerminalScreen extends TerminalScreen implements Runnable, ThreadFactory, TerminalResizeListener
{
    private long lastResizeTime;
    private boolean refreshingFrame;
    private boolean frozen = false;
    private boolean browsing = false;
    private int maxContentSize = 2000;
    private String empty = "                                                                                                                                                                                                         ";
    private char esc = 0x1b;
    private String cursorText = "root@computer " + esc + "[32;m~" + esc + "[0m# ";
    private int realCursorLength = StringUtil.getDisplaySizeOfANSIString(cursorText);
    private int inputRow;
    private int sizeOfLine;
 //   private int inputStartIndex;
    private int inputContentDisplayStartIndex;
    private int cursorDisplayStartIndex;
    private final Object charListLock = new Object();
    private List<Character> charList = new ArrayList<Character>();
    private int currentIndexOfInput;
    private int currentCursorIndexOfTerminal;
    private boolean inputVisibility = true;
    private String displayInputLine = "";

    // 缓存解析后的行，用于快速刷新
    private List<String> cachedBufferLines = new ArrayList<String>();
    private boolean bufferDirty = true;
    private final Object bufferLock = new Object();

    private TextGraphics textGraphics;
    private Text text;
    private boolean resizing;
    private ScheduledExecutorService delayedRefreshExecutor;

    private final Object refreshLock = new Object();

	@Override
	public void onResized(Terminal p1, TerminalSize p2) {
		synchronized (refreshLock) {
			doResizeIfNecessary();
			onResize(p2);
		}
	}

	private void refreshInternal(final RefreshType type) {
		if (System.currentTimeMillis() - lastResizeTime < 10) {
			delayedRefreshExecutor.schedule(new Runnable(){

					@Override
					public void run()
					{
						refreshInternal(type);
						// TODO: Implement this method
					}
					
				
			}
			, 10, TimeUnit.MILLISECONDS);
			return;
		}
		synchronized (refreshLock) {
			try {
				refresh(type);
			} catch (NullPointerException e) {
				// 缓冲区可能损坏，尝试修复并重试一次
				fixBufferState();
				try {
					refresh(type);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void fixBufferState() {
		try {
			TerminalSize size = getTerminalSize();
			TextGraphics tg = newTextGraphics();
			String emptyLine = generateEmptyString(size.getColumns());
			for (int row = 0; row < size.getRows(); row++) {
				tg.putString(0, row, emptyLine);
			}
			// 重新绘制缓存内容
			// ... 可以将 cachedBufferLines 再次写入 ...
			// 标记为脏，下一次 refreshFrame 会完整重绘
			bufferDirty = true;
		} catch (Exception e) {
			// 忽略
		}
	}

	

    @Override
    public Thread newThread(Runnable p1)
    {
        return new Thread(p1, "terminal" + p1.hashCode());
    }

    public FrameTerminalScreen(Terminal term) throws IOException
    {
        super(term);
        text = new Text(maxContentSize);
        new Thread(this, "Virtual-Print-Thread").start();
        delayedRefreshExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					return new Thread(r, "DelayedRefresh");
				}
			});
    }

    public void setMaxContentSize(int size)
    {
        maxContentSize = size;
        text.setMaxContentSize(size);
    }

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
        if (!browsing) {
            bufferDirty = true;
            refreshBuffer();
            refreshFrame(RefreshType.AUTOMATIC);
        }
    }

    public void freeze()
    {
        frozen = !frozen;
        if (!frozen) {
            bufferDirty = true;
            refreshBuffer();
            refreshFrame(RefreshType.AUTOMATIC);
        }
    }

    private LinkedBlockingQueue<Action> printQueue = new LinkedBlockingQueue<Action>(1000);

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
        UpdateInput;
    }

    public void updateInput(String string)
    {
        try
        {
            printQueue.put(new Action(ActionType.UpdateInput, string));
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

    public void initSize()
    {
        this.displayStartPosition = 0;
        this.displayEndPosition = getTerminalSize().getRows() - 1;
        displaySize = displayEndPosition - displayStartPosition;
        bufferDirty = true;
        refreshBuffer();
        refreshFrame(RefreshType.AUTOMATIC);
    }

    private int displayStartPosition = 0;
    private int displayEndPosition = 0;
    private int displaySize = 0;

    public void onResize(TerminalSize p2)
    {
        lastResizeTime = System.currentTimeMillis();
        resizing = true;
        if (this.textGraphics == null)
        {
            return;
        }
        this.displayStartPosition = 0;
        this.displayEndPosition = p2.getRows() - 1;
        displaySize = displayEndPosition - displayStartPosition;
        inputRow = p2.getRows() - 1;
        sizeOfLine = p2.getColumns()-1;

        bufferDirty = true;
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
            refreshFrame(RefreshType.COMPLETE);
        }
		updateCursorPosition();
		//
      //  setCursorPosition(new TerminalPosition(realCursorLength + cursorDisplayStartIndex, inputRow));
    }

    private String generateEmptyString(int columns) {
		// 直接生成所需长度的空格字符串
		return String.format("%" + columns + "s", "");
	}

    public void putCSIStyledString(int column, int inputLine, String string) throws RejectedExecutionException
    {
        if (textGraphics != null)
        {
            textGraphics.putCSIStyledString(column, inputLine, string);
        }
    }

    

    private void internalPrint(String p0)
    {
        synchronized (text) {
            String lastLine = text.getLastLine();
            for (int a = 0; a < p0.toCharArray().length; a++)
            {
                char t = p0.charAt(a);
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
        bufferDirty = true;
    }

    private void setLastLine(String string)
    {
        text.setLast(string);
    }

    private void addNewLine(String string)
    {
        text.appendLine(string);
        if (!frozen /*&& !browsing*/) {
            bufferDirty = true;
        }
    }

    public void pageUp(int p0)
    {
        // 保留空实现
    }

    public void pageDown(int p0)
    {
        // 保留空实现
    }
	
	
	//private PrintStream t=new PrintStream(new FileOutputStream("3.txt"));

    public void refreshBuffer()
    {
        if (frozen /*|| browsing*/) {
            return;
        }
		//如果不在最底下就不刷新
		//t.println(" "+this.text.getSize()+ " "+text.getCurrentIndex()+" "+this.displaySize);
		if(browsing){
			if(this.text.getSize()>text.getCurrentIndex()+this.displaySize+2){
				return ;
			}
		}
        synchronized (bufferLock) {
            if (!bufferDirty) {
                return;
            }
           // long startTime = System.currentTimeMillis();
            List<String> newBuffer = new ArrayList<String>();
            List<String> lastLines = text.getLastLines(displaySize);
            for (int i = 0; i < lastLines.size(); i++)
            {
                String content = lastLines.get(i);
                List<String> parsed = new StringContentParser(content, getTerminalSize().getColumns()).parse();
                for (String p : parsed)
                {
                    newBuffer.add(p);
                }
            }
            if (newBuffer.size() > 0 && newBuffer.get(newBuffer.size() - 1).isEmpty())
            {
                newBuffer.remove(newBuffer.size() - 1);
            }
            while (newBuffer.size() > displaySize)
            {
                newBuffer.remove(0);
            }
            cachedBufferLines = newBuffer;
            bufferDirty = false;
        }
    }

    public boolean scrollLines(int p0) throws RejectedExecutionException
    {
        if (!browsing || frozen)
        {
            throw new IllegalStateException("terminal is not browsing or is frozen");
        }
		//System.err.println(p0+ " "+this.text.getSize()+ " "+text.getCurrentIndex()+" "+this.displaySize);
		//已经滚到最下面了所以不让滚了
		if(p0>0&&this.text.getSize() -text.getCurrentIndex()<=this.displaySize+1){
			return false;
		}
        synchronized (bufferLock) {
            List<String> newBuffer = new ArrayList<String>();
            List<String> lines = text.getLines(Math.max(0, text.getCurrentIndex() + p0), displaySize);
            for (int i = 0; i < lines.size(); i++)
            {
                String content = lines.get(i);
                List<String> parsed = new StringContentParser(content, getTerminalSize().getColumns()).parse();
                for (String p : parsed)
                {
                    newBuffer.add(p);
                }
            }
            cachedBufferLines = newBuffer;
        }
        refreshFrame(RefreshType.AUTOMATIC);
        return true;
    }

    public void refreshFrame(RefreshType type)
    {
		synchronized (refreshLock) {
			refreshingFrame = true;
			// ... 原有的清屏与绘制逻辑 ...
			
        refreshingFrame = true;
        //long startTime = System.currentTimeMillis();
        String emptyLine = generateEmptyString(getTerminalSize().getColumns());
        synchronized (bufferLock) {
            int contentIndex = 0;
            for (int c = displayStartPosition; c < displayEndPosition + 1; c += 1)
            {
                textGraphics.putCSIStyledString(0, c, emptyLine);
            }
            for (int c = displayStartPosition; c < displayEndPosition; c += 1)
            {
                if (contentIndex < cachedBufferLines.size())
                {
                    textGraphics.putCSIStyledString(0, c, cachedBufferLines.get(contentIndex));
                }
                contentIndex++;
            }
        }
        synchronized (charListLock) {
            if (!inputVisibility)
            {
                textGraphics.putCSIStyledString(0, getTerminalSize().getRows() - 1, cursorText + displayInputLine.replaceAll(".", "*"));
            }
            else
            {
                textGraphics.putCSIStyledString(0, getTerminalSize().getRows() - 1, cursorText + displayInputLine);
            }
        }
			refreshingFrame = false;
			refreshInternal(type);
		}
    }

    @Override
    public void startScreen() throws IOException
    {
        textGraphics = newTextGraphics();
        super.startScreen();
        initSize();
        inputRow = getTerminalSize().getRows() - 1;
        this.sizeOfLine = getTerminalSize().getColumns()-1;
        //  setCursorPosition(new TerminalPosition(realCursorLength, inputRow));
		updateCursorPosition();
        refreshFrame(RefreshType.AUTOMATIC);
    }

    public void disableInputVisibility()
    {
        inputVisibility = false;
        refreshFrame(RefreshType.AUTOMATIC);
    }

    public void enableInputVisibility()
    {
        inputVisibility = true;
        refreshFrame(RefreshType.AUTOMATIC);
    }

    public void updateInput()
    {
        updateInputInternal();
    }

    private void updateInputInternal()
    {
      //  String currentLine = "";
        String string = getDisplay();
        char[] array = string.toCharArray();
        int currenLinetSize = 0;
        StringBuilder displayBuilder = new StringBuilder();

        for (int i = 0; i < array.length; i++)
        {
            char character = array[i];
            int displaySizeOfCharacter = 0;
            if (character == 0x1b)
            {
                int length = TerminalTextUtils.getANSIControlSequenceLength(string, i);
                int end = i + length;
                for (int i1 = i; i1 < end; i1++)
                {
                    displayBuilder.append(array[i1]);
                }
                i += length - 1;
                continue;
            }
            else if (TerminalTextUtils.isCharCJK(character))
            {
                displaySizeOfCharacter = 2;
            }
            else
            {
                displaySizeOfCharacter = 1;
            }

            if (currenLinetSize + displaySizeOfCharacter > sizeOfLine)
            {
                updateCursorPosition();
                synchronized (charListLock) {
                    displayInputLine = displayBuilder.toString();
                }
                refreshFrame(RefreshType.AUTOMATIC);
                return;
            }
            if (currenLinetSize + displaySizeOfCharacter == sizeOfLine)
            {
                displayBuilder.append(character);
                updateCursorPosition();
                synchronized (charListLock) {
                    displayInputLine = displayBuilder.toString();
                }
                refreshFrame(RefreshType.AUTOMATIC);
                return;
            }
            else
            {
                displayBuilder.append(character);
                currenLinetSize += displaySizeOfCharacter;
            }
        }
        updateCursorPosition();
        synchronized (charListLock) {
            displayInputLine = displayBuilder.toString();
        }
        refreshFrame(RefreshType.AUTOMATIC);
    }

    private void updateCursorPosition()
    {
        setCursorPosition(new TerminalPosition(realCursorLength + cursorDisplayStartIndex, inputRow));
    }

    private String getDisplay()
    {
        StringBuilder sb = new StringBuilder();
        int offset = inputContentDisplayStartIndex;
        synchronized (charListLock) {
            for (Character c : charList) {
                if (c == null) continue;
                if (offset == 0) {
                    sb.append(c);
                } else if (offset == 1) {
                    if (TerminalTextUtils.isCharCJK(c)) {
                        sb.append(".");
                    }
                    offset = 0;
                } else if (offset > 1) {
                    if (TerminalTextUtils.isCharCJK(c)) {
                        offset -= 2;
                    } else {
                        offset -= 1;
                    }
                }
            }
        }
        return sb.toString();
    }

    public String getCusorText()
    {
        return cursorText;
    }

    public void append(Character character)
    {
        synchronized (charListLock) {
            charList.add(currentIndexOfInput, character);
            currentIndexOfInput++;
            int sizeOfCharacter = TerminalTextUtils.isCharCJK(character) ? 2 : 1;
            if (cursorDisplayStartIndex + realCursorLength >= sizeOfLine)
            {
                inputContentDisplayStartIndex += sizeOfCharacter;
            }
            else
            {
                cursorDisplayStartIndex += sizeOfCharacter;
            }
        }
        updateInput();
    }

    public void delete()
    {
        synchronized (charListLock) {
            if (charList.isEmpty()||currentIndexOfInput - 1<0) return;
            Character character = charList.remove(currentIndexOfInput - 1);
            currentIndexOfInput--;
            int sizeOfCharacter = TerminalTextUtils.isCharCJK(character) ? 2 : 1;
            if (inputContentDisplayStartIndex > 0)
            {
                inputContentDisplayStartIndex -= sizeOfCharacter;
            }
            else
            {
                cursorDisplayStartIndex = Math.max(cursorDisplayStartIndex - sizeOfCharacter, 0);
            }
        }
        updateInput();
    }

    public void goToStart()
    {
        synchronized (charListLock) {
            this.currentCursorIndexOfTerminal = 0;
            this.currentIndexOfInput = 0;
            this.cursorDisplayStartIndex = 0;
            this.inputContentDisplayStartIndex = 0;
        }
        updateInput();
    }

    public void goRight()
    {
        synchronized (charListLock) {
            if (charList.size() == currentIndexOfInput) return;
            char character = charList.get(currentIndexOfInput);
            currentIndexOfInput++;
            int sizeOfCharacter = TerminalTextUtils.isCharCJK(character) ? 2 : 1;
            if (cursorDisplayStartIndex + realCursorLength >= sizeOfLine)
            {
                inputContentDisplayStartIndex += sizeOfCharacter;
            }
            else
            {
                cursorDisplayStartIndex += sizeOfCharacter;
            }
        }
        updateInput();
    }

    public void goLeft()
    {
        synchronized (charListLock) {
            if (currentIndexOfInput == 0) return;
            char character = charList.get(currentIndexOfInput - 1);
            currentIndexOfInput--;
            int sizeOfCharacter = TerminalTextUtils.isCharCJK(character) ? 2 : 1;
            if (inputContentDisplayStartIndex > 0)
            {
                inputContentDisplayStartIndex -= sizeOfCharacter;
            }
            else
            {
                cursorDisplayStartIndex -= sizeOfCharacter;
            }
        }
        updateInput();
    }

    public void wrap(String string)
    {
        clear();
        synchronized (charListLock) {
            for (char c : string.toCharArray())
            {
                charList.add(c);
            }
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
        realCursorLength = StringUtil.getDisplaySizeOfANSIString(cursorText);
    }

    public void clear()
    {
        synchronized (charListLock) {
            charList.clear();
            this.currentCursorIndexOfTerminal = 0;
            this.currentIndexOfInput = 0;
            this.cursorDisplayStartIndex = 0;
            this.inputContentDisplayStartIndex = 0;
        }
        updateInput();
    }

    public String getInput()
    {
        StringBuilder sb = new StringBuilder();
        synchronized (charListLock) {
            for (char c : charList)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Action action = printQueue.take();
                switch (action.type)
                {
                    case PrintLn:
                        internalPrint(action.content + "\n");
                        break;
                    case Print:
                        internalPrint(action.content);
                        break;
                    case UpdateInput:
                        break;
                }
                if (bufferDirty && !frozen /*&& !browsing*/) {
                    refreshBuffer();
                }
                refreshFrame(RefreshType.AUTOMATIC);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    
}
