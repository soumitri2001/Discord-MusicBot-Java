/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appdevSoumitri.javamusicbot;

/**
 *
 * @author SOUMITRI CHATTERJEE
 */
public class Constants {

    /**
     * Please enter your own developer credentials to use the Bot
     */

    // auth tokens
    public static final String bot_token = "BOT_TOKEN";
    public static final String client_id = "DEVELOPER_CLIENT_ID";

    // bot command helpers
    public static final char prefix = '#';

    // API helpers
    public static final String media_url = "https://www.youtube.com/watch?v=";
    public static final String query_api = "https://youtube.googleapis.com/youtube/v3/search?maxResults=4&order=relevance&q=";
    public static final String api_key = "&key=YOUTUBE_API_KEY";
}
