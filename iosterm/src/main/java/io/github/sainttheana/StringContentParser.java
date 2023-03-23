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

import com.googlecode.lanterna.TerminalTextUtils;
import java.util.ArrayList;
import java.util.List;

public class StringContentParser
{
	private String string;

	private int lineSize;

	private String currentLine="";

	private int currenLinetSize;

	private List<String> content=new ArrayList<String>();

	//把一行字符串根据行宽拆成多行
	public StringContentParser(String string, int lineSize)
	{
		this.string = string;
		this.lineSize = lineSize;
	}

	public List<String> parse()
	{
		if(string.length()==0){
			ArrayList<String> a= new ArrayList<String>();
			a.add("");
			return a;
		}
		//System.err.println(string);
		char[] array=string.toCharArray();
		for (int i=0;i < array.length;i++)
		{
			//System.err.println("i " + i);
			char character=array[i];
			int displaySizeOfCharacter=0;
			//System.err.println(character);
			boolean characterVisible=true;
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
				characterVisible = false;
			}
			else if (TerminalTextUtils.isCharCJK(character))//占用两个字符宽度
			{
				displaySizeOfCharacter = 2;
			}
			else//一个字符宽度
			{
				displaySizeOfCharacter = 1;
			}
			if (characterVisible)
			{
				if (currenLinetSize + displaySizeOfCharacter > lineSize)
				{
					//如果把当前的字符拼进去那么会超出长度，理论上当前的是占用两个宽度才会出现
					//所以先把当前的字符串写入，然后当前字符当做下一个字符串的开头
					content.add(currentLine);
					currentLine = "" + character;
					currenLinetSize = displaySizeOfCharacter;
				}
				else if (currenLinetSize + displaySizeOfCharacter == lineSize)
				{
					//刚刚好是一行所以直接写入
					currentLine += character;
					content.add(currentLine);
					currentLine = "";
					currenLinetSize = 0;
				}
				else
				{
					//还没到一行的宽度所以不写入
					currentLine += character;
					currenLinetSize += displaySizeOfCharacter;
				}
			}
			if (i == array.length - 1)
			{
				//到末尾了，也就是最后一个
				if (!currentLine.isEmpty())
				{
					content.add(currentLine);
				}
			}
		}
//		for(String g:content){
//			System.err.println(g);
//		}
		return content;
	}




}
