/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.appdevSoumitri.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SOUMITRI CHATTOPADHYAY
 */

public class GuildAudioManager {
    private static final Map<Snowflake, GuildAudioManager> MANAGERS = new ConcurrentHashMap<>();

    public static GuildAudioManager of(final Snowflake id) {
        return MANAGERS.computeIfAbsent(id, ignored -> new GuildAudioManager());
    }

    private final AudioPlayer player;
    private final AudioTrackScheduler scheduler;
    private final LavaPlayerAudioProvider provider;

    private GuildAudioManager() {
        player = Bot.player;
        scheduler = new AudioTrackScheduler(player);
        provider = new LavaPlayerAudioProvider(player);

        player.addListener(scheduler);
    }

    public static Map<Snowflake, GuildAudioManager> getMANAGERS() {
        return MANAGERS;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public AudioTrackScheduler getScheduler() {
        return scheduler;
    }

    public LavaPlayerAudioProvider getProvider() {
        return provider;
    }
}
