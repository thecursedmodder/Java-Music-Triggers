package net.cursedmodder.javatriggers.triggers.example;

import net.cursedmodder.javatriggers.triggers.songs.BasicSong;
import net.cursedmodder.javatriggers.triggers.songs.Song;
import net.cursedmodder.javatriggers.util.ClientContext;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayerCondition;
import net.cursedmodder.javatriggers.triggers.songs.layer.LayeredSong;

import java.util.ArrayList;
import java.util.List;


public class TriggerSongs {
//Having an a class where you can create your Song variables is a nice touch for readability
    public static List<Song> generalSongs() {
        List<Song> songs = new ArrayList<>();
        //songs.add(new BasicSong("beat.mp3").setStats(200, 100, 1F).setPlayConditions(false, false));
        songs.add(new LayeredSong("beat.mp3", new LayerCondition("beat", ".mp3", 1).setStats(20, 30, 1.0F).setPlayCondition(() -> ClientContext.isBellowHealth(10)), new LayerCondition("beat", ".mp3", 2).setStats(20, 20, 1.0F).setPlayCondition(ClientContext::isPassenger)).setStats(9000, 100, 100, 1.0F).setPlayConditions(false, 0));
        return songs;
    }

    public static List<Song> deathSongs() {
        List<Song> songs = new ArrayList<>();
        //songs.add(new BasicSong("beat.mp3").setStats(200, 100, 1F).setPlayConditions(false, false));
        songs.add(new BasicSong("fallen_man.mp3").setStats(0, 40, 1.0F).setPlayConditions(false, 1));
        return songs;
    }

    public static List<Song> nightSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new LayeredSong("lurkers.mp3",
                new LayerCondition("lurkers", ".mp3", 1).setStats(40, 40, 1.0F).setPlayCondition(() -> ClientContext.isBellowHealth(15)),
                new LayerCondition("lurkers", ".mp3", 2).setStats(40, 40, 1.0F).setPlayCondition(() -> ClientContext.isBellowHealth(15) && ClientContext.canSeeSky()),
                new LayerCondition("lurkers", ".mp3", 3).setStats(40, 40, 1.0F).setPlayCondition(() -> !ClientContext.isBellowHealth(15)),
                new LayerCondition("lurkers", ".mp3", 4).setStats(40, 40, 1.0F).setPlayCondition(() -> ClientContext.isBellowHealth(10))
        ).setStats(100, 100, 1.0F).setPlayConditions(false, 0));
        return songs;
    }

    public static List<Song> rainSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new BasicSong("bloodflow.mp3").setStats(200, 100, 1F).setPlayConditions(false, 0));
        return songs;
    }

    public static List<Song> combatSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new BasicSong("bloodflow.mp3").setStats(100, 100, 1F).setPlayConditions(false, 0));
        return songs;
    }

    public static List<Song> menuSongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new BasicSong("outbreak.mp3").setStats(100, 200, 1F).setPlayConditions(false, 0));
        return songs;
    }

}
