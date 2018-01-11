Pretend You're Xyzzy
===================

A fork of the original Pretend You're Xyzzy. Complete re-write of the frontend, and major changes in the backend. Finished product will have material design, work fairly decently on mobile devices, and have server that is self-contained instead of depending on Tomcat.


Run
===
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

Screenshots
===========
The following are screenshots of work that has been done already or is being worked on as of this writing:

![][screen1]
![][screen2]
![][screen3]
![][screen4]


[screen1]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen1.png?raw=true
[screen2]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen2.png?raw=true
[screen3]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen3.png?raw=true
[screen4]: https://github.com/devgianlu/PYX-Reloaded/blob/material-ui/screenshots/screen4.png?raw=true
