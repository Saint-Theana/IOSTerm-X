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
