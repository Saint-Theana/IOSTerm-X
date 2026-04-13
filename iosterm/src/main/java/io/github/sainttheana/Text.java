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
import java.util.List;
import java.util.ArrayList;

public class Text
{
    public Text(int maxContentSize){
        contents = new LimitedList<String>(maxContentSize);
    }

    private int currentIndex = 0;
    private LimitedList<String> contents;

    public void setMaxContentSize(int size) {
        // 重新创建 LimitedList 并复制现有内容
        LimitedList<String> newContents = new LimitedList<String>(size);
        for (int i = 0; i < contents.size(); i++) {
            newContents.add(contents.get(i));
        }
        contents = newContents;
    }

    public void setCurrentIndex(int currentIndex)
    {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex()
    {
        return currentIndex;
    }

    public String getLastLine()
    {
        if(contents.size() == 0){
            return "";
        }
        return contents.get(contents.size() - 1);
    }

    public List<String> getLastLines(int count)
    {
        List<String> t = new ArrayList<String>();
        int size = contents.size();
        int firstLine = 0;
        for(int i = 0; i <= count; i++){
            if(size - 1 - i < 0){
                break;
            }
            t.add(0, contents.get(size - 1 - i));
            firstLine = size - 1 - i;
        }
        currentIndex = firstLine;
        return t;
    }

    public void setLast(String string)
    {
        contents.set(contents.size() - 1, string);
    }

    public void appendLine(String line){
        contents.add(line);
    }

    public List<String> getLines(int start, int count){
        if(start >= contents.size()){
            start = contents.size() - 1;
        }
        List<String> t = new ArrayList<String>();
        for(int i = 0; i < count; i++){
            if(start + i >= contents.size()){
                break;
            }
            t.add(contents.get(start + i));
        }
        currentIndex = start;
        return t;
    }

    public int getSize(){
        return contents.size();
    }
}
