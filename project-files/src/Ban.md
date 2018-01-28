# Ban
This handler provides the basic algo that processes the request to ban a user, usually from the in-game console as an admin user who typed in ```/ban user``` or some similar command. The way the server handles this is as follows:

1. Get the list of users currently connected to the server.
2. Begin processing the ```/ban``` command and its parameters:
    - Check to make sure the user has admin rights on the server.
        - If they don't, give an error that states the user is not an admin.
    - Start concatenation process - inputs here get translated to a JSON response that is read/interpreted by the client.
        - Set a variable (in this case, ```nickname```) to the one entered as the parameter of ```/ban```
        - If the nickname parameter is ```null``` or otherwise unspecified, exit with ```No nick specified```
        - Declasre an empty string, ```banIp```.
        - Create a variable of custom ```User``` type, ```kickUser```
            - Of the currently connected users we want to ban, match this up with the parameter of the ```/ban``` command.
            - Basically, by this point, we have a name for the user we want to kick!
        - Check ```kickUser``` for validity, and decide which route to take to rid that scum from the server:
            - If the returned value of ```kickUser``` is valid, do the following:
                - Set ```banIP``` to the IP of the user in question
                - Send a message via the ```LongPoll``` servlet to the client with a notif they've been kicked.
                - Remove the user in question from the list of connected users on the server.
                - To everyone else: Send a message of who banned who.
            - If ```kickUser``` doesn't come back with anything proper, be a savage and directly ban the nick itself, even if it doesn't exist. 
        - After determining ```banIP```, the nick or IP address gets added to the internal list of banned users.
    - End concatenation process. This handler doesn't return any JSON by itself, however it may return an error to the client if one happens to arise.
                
