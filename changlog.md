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