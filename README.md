<p align="center"><img src="src/main/ic_launcher-web.png" width="200"/></p> 
<h1 align="center">Watcher</h2>
<h4 align="center"><i>A crippled screenshot utility for Android that can't even save the captured images</i></h4>

<p align="center">
<a href="https://github.com/lczx/watcher" alt="GitHub release"><img src="https://img.shields.io/github/release/lczx/watcher.svg" /></a>
<a href="https://opensource.org/licenses/MIT" alt="License: MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" /></a>
</p>

**Watcher** shows an overlay on the screen that you can use to capture
snapshots using the Android MediaProjection API, the images are cropped
to the center square of the screen and kept in memory.

### Why?

First of all, this is a way for myself to (try to) develop a proper,
although useless, Android application; hopefully learning new things in the
meantime.

This was made for some people I know that play a game named *Ingress*,
if you are frustrated by "Glyph Hacking" this may be for you.

It is needless to say that this surely violates the game's EULA, in
fact they might ban your account or better, `FLAG_SECURE` their content
so there is no way I can screenshot their things without root.

**See [Contributing](#contributing) for more!**

### Usage

Enable the overlay and touch the pane over the status bar to start a
capture session; now use the new fancy screen capture button to take
screenshots.

Long-pressing the capture button switches to browse mode where the captured
images are translucently overlaid at the same location they were taken.
In this mode, long press the forward button to return taking screenshots
and forget the previously captured sequence.

Touching again the pane over the status bar exits the session,
long touching it opens the application.

### Contributing

Despite this project's initial purpose, I am *very* interested to new ideas:
need a more generic screenshot/video capture utility or something more suited for your needs? Fill in
[an issue](https://github.com/lczx/watcher/issues/new) so we can arrange
something; the same holds true if you find a bug,
[report it](https://github.com/lczx/watcher/issues/new)!

If you like coding and are willing to help,
[pull requests](https://github.com/lczx/watcher/pulls) are always welcome.

### License

This application is under the MIT license, however the logo font is under a
[Font Software License Agreement - Non-Commercial](src/main/assets/fonts/Gravedigger_license.html)
license by Chequered Ink Ltd.
