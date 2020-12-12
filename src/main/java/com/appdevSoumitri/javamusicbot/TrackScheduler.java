/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdevSoumitri.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * @author SOUMITRI CHATTOPADhYAY
 */

public class TrackScheduler implements AudioLoadResultHandler, AudioEventListener {

    private final AudioPlayer player;

    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        // found an audio source to play
        player.playTrack(audioTrack);
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        // found multiple tracks from some playlist
    }

    @Override
    public void noMatches() {
        // did not find any audio to extract
    }

    @Override
    public void loadFailed(FriendlyException e) {
        // could not parse an audio file
        e.printStackTrace();
    }

    @Override
    public void onEvent(AudioEvent audioEvent) {

    }
}
