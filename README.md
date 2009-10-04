Plugins and Scripts for Art of Illusion
=======================================

This repository contains all my plugins and scripts for Art of Illusion.
I decided to create one single repository because I don't make changes
to the files in here on a regular basis -- and creating one separate
repository for each plugin or script (just a bunch of files) seems a
little pointless to me.


Building
--------

Just change to the plugin you wish to build and issue:

	$ ant

Be aware, though, that you'll need to copy the files "ArtOfIllusion.jar"
and "PreferencesPlugin.jar" from your current AoI installation into the
"jars" subdirectory. They can be symbolic links, of course.

Cleaning up one plugin:

	$ ant clean

Scripts don't need to be compiled.


Creating a snapshot
-------------------

A snapshot is an archive that will contain all compiled plugins or
scripts in their current version. You can create them by issuing

	$ ./create-snapshot.sh

from the repo's root directory. The archives will end up in "archives"
and their names will contain the current date (something like
`Plugins-2009-09-25.tgz`).


Contact
-------

[German AoI Board](http://www.aoi-board.de/)
[FriendlySkies Forum](http://www.friendlyskies.net/aoiforum/)
