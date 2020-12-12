package com.appdevSoumitri.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Bot {

    private static final Map<String,Command> commands = new HashMap<>();

    static {
        commands.put("ping", event -> Objects.requireNonNull(event.getMessage()
                .getChannel().block())
                .createMessage("Pong!").block());
    }

    public static AudioPlayer player;
    private static AudioProvider provider;
    private static AudioPlayerManager playerManager;
    private static AudioTrackScheduler scheduler;

    private static void initializeLavaPlayer() {
        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize. It is not important to understand
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

        // Create an AudioPlayer so Discord4J can receive audio data
        player = playerManager.createPlayer();

        // We will be creating LavaPlayerAudioProvider in the next step
        provider = new LavaPlayerAudioProvider(player);

        scheduler = new AudioTrackScheduler(player);
        player.addListener(scheduler);
    }

    private static String getSearchResultFromYoutube(String[] query) {
        String search="",urlStr=Constants.media_url;
        for(int i=0;i<query.length-1;i++) {
            search+=query[i]+"%20";
        }
        search+=query[query.length-1];
        String api = Constants.query_api+search+Constants.api_key;

        // JSON parsing
        try {
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responsecode = conn.getResponseCode();
            if(responsecode == 200) {
                String inline = "";
                Scanner sc=new Scanner(url.openStream());
                while(sc.hasNext()) {
                    inline+=sc.nextLine();
                }
                JSONParser parser = new JSONParser();
                JSONObject root = (JSONObject) parser.parse(inline);
                JSONArray items = (JSONArray) root.get("items");
                JSONObject firstObj = (JSONObject) items.get(0);
                if(firstObj != null) {
                    String videoID = (String) ((JSONObject)firstObj.get("id")).get("videoId");
                    urlStr+=videoID;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlStr;
    }

    public static void main(String[] args) {

        initializeLavaPlayer();

        //joining voice channel command: #join
        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final VoiceChannel channel = voiceState.getChannel().block();
                    if (channel != null) {
                        // join returns a VoiceConnection which would be required if we were
                        // adding disconnection features, but for now we are just ignoring it.
                        channel.join(spec -> spec.setProvider(provider)).block();
                    }
                }
            }
        });

        // setting up #play command
        commands.put("play",event -> {
            // query[1] is a media link (youtube/SoundCloud etc as supported by LavaPlayer)
           final String[] query = event.getMessage().getContent().split(" ");
           if(query.length >= 2) {
               // query is command followed by media link
               String[] q=new String[query.length-1];
               for(int i=1;i<query.length;i++) {
                   q[i-1]=query[i];
               }
               String trackUrl = getSearchResultFromYoutube(q);
               player.setPaused(false);
               playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
                   @Override
                   public void trackLoaded(AudioTrack audioTrack) {
                        if(player.getPlayingTrack() == null) {
                            player.playTrack(audioTrack);
                        } else {
                            scheduler.play(audioTrack);
                        }
                   }

                   @Override
                   public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        for(AudioTrack track : audioPlaylist.getTracks()) {
                            boolean isPlaying = scheduler.play(track);
                            System.out.println(isPlaying);
                        }
                   }

                   @Override
                   public void noMatches() {
                       (Objects.requireNonNull(event.getMessage()
                               .getChannel()
                               .block())
                       ).createMessage("It seems the track source is invalid !").block();
                   }

                   @Override
                   public void loadFailed(FriendlyException e) {
                        e.printStackTrace();
                       (Objects.requireNonNull(event.getMessage()
                               .getChannel()
                               .block())
                       ).createMessage("Exception occurred !").block();
                   }
               });
           }
        });

//       #skip command
        commands.put("skip",event -> {
            scheduler.skip();
            (Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
            ).createMessage("Current song skipped !").block();
        });

//        #pause command
        commands.put("pause",event -> {
            player.setPaused(true);
            (Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
            ).createMessage("Paused !").block();
        });

//        #resume commad
        commands.put("resume",event -> {
            player.setPaused(false);
            (Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
            ).createMessage("resumed !").block();
        });

//        #stop command
        commands.put("stop",event -> {
            player.stopTrack();
            (Objects.requireNonNull(event.getMessage()
                    .getChannel()
                    .block())
            ).createMessage("Music stopped !").block();
        });

        // connecting to discord web-sockets using client token
        final GatewayDiscordClient client = DiscordClientBuilder.create(Constants.bot_token)
                                                                .build()
                                                                .login()
                                                                .block();
        assert client != null;

        // when a message is sent in the channel
        client.getEventDispatcher().on(MessageCreateEvent.class)
                // subscribe is like block, in that it will *request* for action
                // to be done, but instead of blocking the thread, waiting for it
                // to finish, it will just execute the results asynchronously.
                .subscribe(event -> {
                    final String content = event.getMessage().getContent(); // 3.1 Message.getContent() is a String

                    for (final Map.Entry<String, Command> entry : commands.entrySet()) {
                        // We will be using '#' as our "prefix" to any command in the system.
                        if (content.startsWith(Constants.prefix + entry.getKey())) {
                            entry.getValue().execute(event);
                            break;
                        }
                    }
                });

        client.onDisconnect().block();

    }
}
