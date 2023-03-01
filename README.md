# IOSTerm-X
[![](https://jitpack.io/v/Saint-Theana/IOSTerm-X.svg)](https://jitpack.io/#Saint-Theana/IOSTerm-X)
![gif](https://raw.githubusercontent.com/Saint-Theana/IOSTerm-X/master/xxx.gif)

# An I/O seperated terminal Text-Gui interface based on [lanterna](https://github.com/mabe02/lanterna)

To add this library:
[jitpack](https://jitpack.io/#Saint-Theana/IOSTerm-X)
```groovy
lallprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
	
dependencies {
	implementation 'com.github.Saint-Theana:IOSTerm-X:1.0.7'
}
```

# implemented feature:
 - [x] 1.Chiness Japeness Korean character length adapt.
 - [x] 2.Auto word wrapping.
 - [x] 3.Page scroll up/down
 - [x] 4.Resize auto adapt.
 - [x] 5.Input visibility control.
 - [x] 6.Input history.
 - [ ] 7.Page up/down.

# How to use

## 1: create a Input reader by

```java
BasicTerminal.InputReader inputReader = new BasicTerminal.InputReader(){
    @Override
	public void read(String input)
	{
	    //everytime user type enter,this method will be called with input 
	}
}
```

## 2: start the Terminal
```java
BasicTerminal terminal = new BasicTerminal(this);
//you can decide if terminal overrides System.in/System.out/System.err by using
terminal.setOverrideStandardErr(true);
terminal.setOverrideStandardOut(true);
terminal.setOverrideStandardIn(true);
//these are disabled by default.
//important: if you chose not to enable overriding System.err,standard err will mess up the terminal.so you must seperate it by using "java ..... -jar xxxx.jar 2>err.log"
//if you chose to enable overriding System.err,when your app or terminal crashed while terminal is starting,you might not be able to recieve any infomation.
terminal.process();
```

## 3: how to write message to the terminal?
```java
//if terminal overrides System.out and System.err
System.out.print("Hello world.");
System.out.printn("Hello world.");
System.err.print("Hello world.");
System.err.printn("Hello world.");
//if not
terminal.out.print("hello world");
terminal.out.println("hello world");
```

## 4: how to read input manually?
```java
//there are two ways to do that
//1.intercept terminal's reader.
terminal.interceptReader(new BasicTerminal.InputReader(){
	@Override
	public void read(String input)
	{
		//here you can get the input
		//remember after you get what you want,call terminal.releaseReader()
	}
});


//2.if System.in was overriden by the terminal,you can use Scanner
//notice:this does not behave like standard input,scanner will read all current input line even before you called new Scanner(System.in);
//notice:since there is no way to know when user uses a Scanner,so the input will also call terminals reader.
//important:arrow up/down will mess up input content.
Scanner s=new Scanner(System.in);
System.out.println(s.nextLine());
```

## 5: anything else?
```java
//password input mode
terminal.enableInputVisibility();
terminal.disableInputVisibility();
```

## After that,a text gui interface will open.
## remember,do not use gradle run to test it,do not ask why,try it and you will get why.

## License
```
Copyright (C) 2023-2025  Saint-Theana

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
```