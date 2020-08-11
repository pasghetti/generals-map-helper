# Generals.io Map Creation Helper

The Generals.io Map Creation Helper allows users to easily create and publish maps on [generals.io](http://generals.io), a multiplayer online game. This helper seeks to expand the functionalities of the map creator at [generals.io/mapcreator](generals.io/mapcreator), adding helpful tools such as:

- a paint function that allows users to edit the map by dragging their mouse instead of individually clicking squares
- a rectangular fill function that allows users to add large sections of uniform terrain to their map
- a select function that can cutting, copying, and pasting of regions on the map
- a save/load function that can save maps to be edited later or shared with others

## Getting Started

This project uses Java 14. The Java Development Kit can be downloaded here:

https://www.oracle.com/java/technologies/javase-jdk14-downloads.html

This project also uses Apache Maven, and can be imported as an Eclipse Maven Project.

The jar for the project can be built with the command `mvn clean install assembly:single` if using Maven directly; otherwise, if you are using Eclipse, you will have to select "Maven build" from the "Run as..." menu. Once you have the prompt to configure the run, you can type "clean install assembly:single" as the build goals. The jar for the project should appear in the "target" folder, titled "GeneralsMapHelper-0.0.1-SNAPSHOT-jar-with-dependencies.jar".

## Usage

To execute the jar of the project, you can either build the jar yourself using the above instructions, or you can just use the pre-built jar file that is in the dist folder. If you have Java installed, double-clicking the jar file should run it.

### Important controls

- Shift + Click + Drag Mouse: move the map view around
- Ctrl+x, Ctrl+c, Ctrl+v: Cut, copy, and paste selected region (respectively)
- Scroll: zoom in/out of map

### Key

- Mountain = black square
- General/Spawn = black square + red circle on top
- City = gray circle
- Neutral army = gray square
- Swamp = dark green square

### Setting user ID to publish map

When you try to publish the map, you will see that you must set an environment variable named "GeneralsID" to your user ID. In order to find your user ID:

1. Go to [generals.io](http://generals.io).
2. Open the developer tools ("inspect element").
3. Go to the console tab and scroll all the way up.
4. On the second line, there will be your user ID. Note that the first line has a warning about how you must not share your user ID with others. This is because other people can play games as you/publish maps as you/do anything as you on generals.io if they know your ID.

Once you have the environment variable set, you will be able to publish maps. Note that if you are using Eclipse or some other IDE to build and test this project, you will have to restart the IDE after changing the environment variables.

## Contributors

- pasghetti (me)