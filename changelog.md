1.0.1
-Fixed rare crash from a multithreading issue in Channel
-Made the debug window false by default
-Changed all triggers in the example folder to have TriggerExample after their name.

1.0.2 
-added Minecraft's master volume to the player master volume calculation.
-fixed issue with not being able to load songs from another mod. >:(

1.0.3
-integrated the canSongPlay boolean into the getSong function for advanced users.
--Context Updates--
-added speedIsAtOrBeyond check
-added worldHeight check
-added recentlyDamage check
-added isFalling check
-added isUnderWater check
-added onGround check
-added isClimbing check
-added isSwimming check
-added isDayCountAtOrBeyond check
1.0.4
-switched the loading boolean to atomic from volatile for a major stability increase.

1.0.5
-moved trigger state monitors to the TriggerBase class. Now triggers are automatically added to the debug window
-fixed continue function for songs. If a song is fading out and becomes playable it will fade back in from where it's at.
-fixed triggers with multiple songs crashing when a song ends.
-added isDay context check
-added isFullMoon and isMoonPhase context check.

1.1.0
-reverted back to manual trigger monitor adding (Sucks, but it's not too hard. Will try to fix later)
-fixed issue with audio player crashing due to false trigger replacement checks
-fixed small issues.
-added a delay before a trigger can play again.
--Context Updates--
-split up day and night context into 4
-added dawn, day, dusk, and night checks
-added home check
-added biome check (This might be in the last update idk)
