# IOSTerm-X
[![](https://jitpack.io/v/Saint-Theana/IOSTerm-X.svg)](https://jitpack.io/#Saint-Theana/IOSTerm-X)

An I/O seperated terminal Text-Gui interface based on lanterna

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
	implementation 'com.github.Saint-Theana:IOSTerm-X:1.0.2'
}
```


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
   terminal.process();
```

## 3: how to write message to the terminal?
```java
    //terminal will override System.out and System.err
    //have fun.
    System.out.print("Hello world.");
    System.out.printn("Hello world.");
    System.err.print("Hello world.");
    System.err.printn("Hello world.");
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