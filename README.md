Quagga
======

A [Google Cast][1] app built at [DevFest Berlin 2013][2] at the "Retro" Hackathon.

How to get it running
---------------------

1. Both, the sender and the receiver must be on the same network (reachable via *ping*).
2. The sender app must be white-listed on the receiver (see below).
3. The *protocol* of the receiver must match the *namespace* configured on the sender.


How to white-list the sender
----------------------------

Send the content of `whitelist.json` via curl to the sender:

    {
        "appName":"WhiteNoise",
        "appUrl":"http://192.168.43.183:8000",
        "protocols":["ramp"]
    }

This will register the app on the receiver.

    $ curl -vX POST http://192.168.43.172:8008/registerApp -d @whitelist.json -H "Content-Type: application/json"


Disclaimer
----------

The initial version of the app and website has been developed at the hackathon
within a few hours. Therefore, please do not expect any code quality or design.


[1]: http://developers.google.com/cast/
[2]: http://devfest-berlin.de/#/2013/about
