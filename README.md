Quagga
======

A [Google Cast][1] app built at [DevFest Berlin 2013][2] at the "Retro" Hackathon.


Screenshot
------------------
The app shows white noise on the receiver ...

![White noise][3]


How it works
------------

The graphic visualizes how the components communicate with each other.

![Architecture][4]


How to get it running
---------------------

1. Both, the sender and the receiver must be on the same network (reachable via *ping*).
2. The content (white noise) is served by a web server - you need to start it (see below).
3. The sender app must be white-listed on the receiver (see below).
4. The *protocol* of the receiver must match the *namespace* configured on the sender.


How to serve the content
------------------------
Open a terminal and change to the `QuaggaJS` directory. There you have to start a web server to serve the content.

    $ cd QuaggaJS
    $ python -m SimpleHTTPServer


How to white-list the sender
----------------------------

Send the content of `whitelist.json` via curl to the sender:

    {
        "appName":"WhiteNoise",
        "appUrl":"http://192.168.43.183:8000",
        "protocols":["ramp"]
    }

The `appUrl` must match the ip address and port of the webserver serving the content.
The following command will register the app on the receiver.

    $ curl -vX POST http://192.168.43.172:8008/registerApp -d @whitelist.json -H "Content-Type: application/json"


Disclaimer
----------

The initial version of the app and website has been developed at the hackathon
within a few hours. Therefore, please do not expect any code quality or design.


[1]: http://developers.google.com/cast/
[2]: http://devfest-berlin.de/#/2013/about
[3]: http://github.com/daus-salar/quagga/raw/master/screenshot.png
[4]: http://github.com/daus-salar/quagga/raw/master/architecture.png
