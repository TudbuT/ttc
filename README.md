# [Website](https://tudbut.de:83#ttc)

TTC Client
==========

This client is only meant to be used on anarchy servers!!!

DONT ACTIVATE ANY OTHER AUTOTOTEM ALONG THIS ONE, it will glitch it out!

ClickGUI opens with COMMA

The first client to have a SeedOverlay ( / Seed Overlay ) module. For free. (Konas skidded it)

## WebServices explaination

TTC tracks the following data:

- Minecraft name
- Last time the client was started
- Last time the client was closed
- Playtime with the client installed
- Analytics about versions:
  - Version number
  - Client name (in case of fork)
  - Github repo (in case of fork)

TTC does not track:

- Your minecraft login data (some say the password 
key in the api is a Minecraft password, but in reality, it is
only even a thing in premium accounts, and is used to store a 
double-hash of the selected password for logging into TTC premium)
- Your minecraft session
- Your IP (the ip key in user records is still showing REDACTED so
very old ones don't get leaked, now, it will always be 127.0.0.1 due
to the DDOS protection. it was originally used for IP banning, however,
there were incidents where IPs were leaked)
- Your windows username (unlike future, impact, pyro, and many others)
- Your GPS (unlike impact)


## How to fork

(assuming you've already forked and cloned the repo)

1. Change BRAND and REPO variable in TTC.java
2. Change repo link in makerelease.sh
3. Use makerelease to make a new version:
    1. Open WSL (or use a linux terminal)
    2. Run `bash makerelease.sh`
    3. Enter the new version you want and press enter (something like v1.0.0a)
    4. Edit the message (this requires vim unless you customize the script)
    5. Quit the editor
    6. Your browser will open
    7. Wait for the script to tell you to get ready to publish
    8. Upload build/reobfJar/ttc.jar (you can change this name in build.gradle) and wait for that to finish
    9. Paste the message into the release and add the tag and name (that should simply be the version number)
    10. Press enter in the terminal
    11. Wait until the script is done executing
    12. Press Publish Release in the browser
