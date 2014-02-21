A Simple, Easy to Use, Thread-safe Finite State Machine in Java
===============================================================

From [Wikipedia](http://en.wikipedia.org/wiki/Finite-state_machine "Wikipedia: Finite State Machine"):

> A finite-state machine (FSM) or finite-state automaton (plural: automata), or simply a state machine, is a mathematical model of computation used to design both computer programs and sequential logic circuits. It is conceived as an abstract machine that can be in one of a finite number of states. The machine is in only one state at a time; the state it is in at any given time is called the current state. It can change from one state to another when initiated by a triggering event or condition; this is called a transition. A particular FSM is defined by a list of its states, and the triggering condition for each transition.

This was my first delve into Java after spending over 10 years writing in almost exclusively C#. The classes in the code are building blocks for a finite state machine. A developer can build a working state machine by creating instances of the classes and supplying relevant action methods and trigger validation rules at runtime. A small sample application (a coin-operated turnstile simulation) is included.

This project is licensed under the MIT license: essentially, anyone can do anything at all with it, but whatever you do I'm not responsible. You are very welcome to make use of this code, to fork it or send me pull requests; if you do use it, then it would make my day if you drop me a message to let me know you found it useful.

My blog contains a short overview of the code at http://www.tigranetworks.co.uk/blogs/electricdreams/java-finite-state-machine/