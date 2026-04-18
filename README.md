Java Music Control (JMC) is an open-source Minecraft mod that makes it easy to create advanced, dynamic music.
You will need to create your own mod to use this one. However, if you are new to modding, this is a great place to start. 
As for experienced modders, this will likely be the easiest library you will use.
The documentation for this library mod will be immense, with the soon-to-come video tutorials breaking down everything you will need to create your own music mod for JMC.

Features: 
JMC uses booleans (True or False) for determining if a trigger can play. Each trigger also has a priority and other parameters.
JMC has a decent amount of Context for creating triggers. Context is essentially just safe to access booleans.

Multilayered audio: different instruments can be played during different situations in real time!

Underwater muffling

Fading: Both fade in and fade out; this is done on a per-song basis rather than on a trigger basis.

Start and End delay: You can set the amount of time before a trigger can play or be replaced.

Force interrupt: this can allow a trigger to stop and replace another trigger immediately; it skips fade-outs.

Must Finish: Songs each have a must finish parameter, so even if a trigger has a higher priority than the current one, it plays out.

Pause Volume: You can have the volume change depending on whether it is paused.

Song Start Time: Each song can have different start times, which can also be adjusted dynamically, like most of the values.

File Support: Currently, it supports MP3 and WAV at 44.1khz. I recommend downloading Audacity for converting to the correct format. 


Getting started, Experienced Users:

You're going to want to use [cursemaven](https://www.cursemaven.com). Follow the instructions for adding the repository.
Navigate to Curseforge for [JMC](https://www.curseforge.com/minecraft/mc-mods/java-music-triggers/files/all?page=1&pageSize=20&showAlphaFiles=show). Select the latest version of JMC 
and look for Curse Maven Snippet. Make sure the version matches the one you're creating for! Then, copy and paste it into your build.gradle file in the dependency category.

Creating Triggers:

There are multiple constructors you can choose from. I recommend the one with priority only. It makes it easier to keep track of which triggers should take priority over others.
Then you need to override the canPlay method and make sure you use super.canPlay in your return statement. If no songs can be played, the trigger can't play. These are the [context booleans](https://github.com/thecursedmodder/Java-Music-Triggers/blob/main/src/main/java/net/cursedmodder/javatriggers/util/ClientContext.java)

<img width="906" height="439" alt="image" src="https://github.com/user-attachments/assets/165cb1e9-5102-4c87-977d-47cf4db43112" />

Now you can register them in your mod Constructor class.
<img width="1008" height="376" alt="image" src="https://github.com/user-attachments/assets/38f206c1-aa62-4e1e-957e-3aa2db1dc70f" />


Then we also have layered songs, which is probably the biggest feature in this mod.
<img width="1301" height="224" alt="image" src="https://github.com/user-attachments/assets/b36e92f0-3c05-46a8-aab3-36d933ec5665" />

For more details, go to the [wiki](https://github.com/thecursedmodder/Java-Music-Triggers/wiki)




