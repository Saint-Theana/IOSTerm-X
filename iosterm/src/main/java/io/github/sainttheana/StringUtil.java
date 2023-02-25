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

public class StringUtil
{
	


	public static int getDisplaySizeOfANSIString(String string)
	{
		char[] array=string.toCharArray();
		int totalSize=0;
		for (int i=0;i < array.length;i++)
		{
			//System.err.println("i "+i);
			char character=array[i];
			int displaySizeOfCharacter=0;
			//System.err.println(character);
			if (character == 0x1b)//esc ansi控制字符，这玩意不显示在终端里
			{
				int length=TerminalTextUtils.getANSIControlSequenceLength(string,i);
				//把这个字符拼进去然后还要把对应长度的控制字符写进去并且忽略长度
				//System.err.println(length);
				//currentLine+=character;
				int end=i + length;
				for(int i1=i;i1<end;i1++){
					//System.err.println("i1 " + i1);
					
				}
				i+=length-1;
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
			totalSize+=displaySizeOfCharacter;
		}
		return totalSize;
	}
}
