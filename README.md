Pretend You're Xyzzy
===================
A fork of the original Pretend You're Xyzzy. Complete re-write of the frontend, and major changes in the backend. Finished product will have material design, work fairly decently on mobile devices, and have a server that is self-contained instead of depending on Tomcat.

Features
--------
We are nowhere near close to "done." It's been amazing to watch this thing climb. However, while we're working, we'll add new photos and explanations here so (eventually) we'll be able to convince you to use our fork. The main advantage to using this over the original or another fork are as follows:

* Improved back-end
-* Easier installation using a single jar file
-* No dependency on containers like Tomcat
* Completely new frontend 
-* Full support for mobile device browsers (the current one simply doesn't work right in a browser on your phone)
-* Material Design through MDC-Web
-* See more below!

The following features have been implemented thusfar:

### Removal of the BULLSHIT the original put you through to get to the games!
This was a pain in the ass. The first screen the server takes you to is now the one with a place at the top to get a nick and start playing! 

![][screen1]

### No Games is absolutely fine
The original PYX had issues: It would spit errors along the lines of "Game 0 not found", it appeared as if at least one game should be open at all times if the server is running. We fixed this, and added a beautiful placeholder to make sure everyone knows no games exist!

![][screen2]

### ...but some may be necessary to have a good time!!
![][screen9]

### New Game!
We cleaned up that ugly-ass screen the OG PYX had. It's been replaced with a nicer modal dialog (scrollable!) and - unlike its predecessor - has a built-in way of adding Cardcast decks via code straight from the UI. 

![][screen3]
![][screen4]

Selecting the cards from the original PYX is also included, with Material checkboxes to make life complete!!
![][screen6]

Before we add a Cardcast to your game, we want to make sure it's the exact one you want, by the author you chose.
![][screen7]

When this appears, the deck you selected and confirmed should now be an official part of your game!!
![][screen8]

### Oooooooh Shiny!!!
We've added custom colors from Google's Material palette. You may have noticed the ever-changing scheme in the photos above. The top-right "gear" icon opens this dialog. The screenshots were all taken within seconds of each other thanks to this screen. It also has a dynamic preview so you can see what your choices will look like when applied!!

![][screen5]

Run
---
While the server and current WebContent can be run, the game itself is unplayable. However, if you want to to test the work that IS done so far, you can do so via the following commands:

```sh
git clone https://github.com/devgianlu/PYX-Reloaded.git
cd PYX-Reloaded
mvn clean package
sudo java -jar $HOME/PYX-Reloaded/target/PYX-jar-with-dependencies.jar
```

**DO NOT** close the terminal after the server reports that it successfully loaded X number of cards. This will shut the server down.

**NOTE**: These are instructions for Linux users. **IF** you happen to be on Windows, you can go to the Windows Store and get a CLI distribution that will allow you to run the above commands from Windows.

**PREREQUISITES**: You need to have JDK8 (OpenJDK and the like) and Apache Maven installed and available in your path. Please look up which packages to install, as it will be heavily dependent upon which distro you run.

After, run:

```
ifconfig
```

from your terminal. Find the one section that has ```<UP,BROADCAST,RUNNING,MULTICAST>``` and copy the IP address, which should be marked as *inet*. Paste that IP address in your browser. If all is working smoothly, you should be taken to a page asking for you to enter a username.

[screen1]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen1.png?raw=true
[screen2]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen2.png?raw=true
[screen3]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen3.png?raw=true
[screen4]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen4.png?raw=true
[screen5]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen5.png?raw=true
[screen6]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen6.png?raw=true
[screen7]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen7.png?raw=true
[screen8]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen8.png?raw=true
[screen9]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen9.png?raw=true
