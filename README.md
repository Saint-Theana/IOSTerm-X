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
	implementation 'com.github.Saint-Theana:IOSTerm-X:1.0.25'
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

## 1: start the Terminal
```java
BasicTerminal terminal = new BasicTerminal();
//you can decide if terminal overrides System.out/System.err by using
terminal.setOverrideStandardErr(true);
terminal.setOverrideStandardOut(true);
//these are disabled by default.
//important: if you chose not to enable overriding System.err,standard err will mess up the terminal.so you must seperate it by using "java ..... -jar xxxx.jar 2>err.log"
//if you chose to enable overriding System.err,when your app or terminal crashed while terminal is starting,you might not be able to recieve any infomation.
terminal.process();
```
## After that,a text gui interface will start.
## remember,do not use gradle run to test it,do not ask why,try it and you will get why.


## 2: how to write message to the terminal?
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

## 3: how to read input manually?
```java
//method 1: use the Scanner.
//notice:this does not behave like standard input,scanner will read all current input line even before you called new Scanner(System.in);
Scanner s=new Scanner(System.in);
System.out.println(s.nextLine());

//method 2: use the InputDistributor
//reading input in multi thread at the same time will be conflictive
//if you indeed are doing so,InputDistributor.is recommended
//start InputDistributor thread.
new InputDistributor.start()
//create an inputreader and register it.
InputDistributor.registerHighPriority(new InputDistributor.InputReader(){
    @Override public void read(String input){
        
    }
});
//InputDistributor will call only one InputReader with highest priority.
//if you want an inputreader to always be called.
//InputDistributor.registerAlways()
//InputDistributor.unregister() to unregister readers.
//it's basically still based on Scanner,but using Scanner in one thread will be safe.
```

## 4: how do I scroll the output?
```
first you need to type "ctrl + b" to enter browse mode.
and then use up/down
how to exit browse mode?type "ctrl + b" again or type esc.
and if you wanna make terminal stop updating,type "ctrl + f",type agin to exit,same with esc.
```

## 5: anything else?
```java
//password input mode
terminal.enableInputVisibility();
terminal.disableInputVisibility();
```

## License
```
Copyright (C) 2023  Saint-Theana

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
