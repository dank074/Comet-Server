package com.cometproject.server.game.rooms.avatars.misc;


import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.GameEngine;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

public class FilterManager {
    public List<String> blacklistedWords = new ArrayList<>();
    public List<String> whitelistedWords = new ArrayList<>();
    private Logger log = Logger.getLogger(FilterManager.class.getName());

    public FilterManager() {
        // TODO: Get Julien to send the database.. :P
        init();
    }

    public void init() {
        if (blacklistedWords.size() >= 0) {
            blacklistedWords.clear();
        }

        try {
            ResultSet wordResult = Comet.getServer().getStorage().getTable("SELECT word FROM wordfilter WHERE type='blacklist'");

            while (wordResult.next()) {
                blacklistedWords.add(wordResult.getString("word"));
            }
        } catch (Exception e) {
            log.error("Error while loading blacklisted words", e);
        }

        log.info("Loaded " + blacklistedWords.size() + " blacklisted words");

        if (whitelistedWords.size() >= 0) {
            whitelistedWords.clear();
        }

        try {
            ResultSet wordResult = Comet.getServer().getStorage().getTable("SELECT word FROM wordfilter WHERE type='whitelist'");

            while (wordResult.next()) {
                whitelistedWords.add(wordResult.getString("word"));

            }
        } catch (Exception e) {
            log.error("Error while loading whitelisted words", e);
        }

        log.info("Loaded " + whitelistedWords.size() + " whitelisted words");

    }

    public static String removeAccents(String text) {
        return text == null ? null
                : Normalizer.normalize(text, Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public boolean filter(String message) {
        if (message.length() < 3)
            return false;
        for (String AllowedString : whitelistedWords) {
            if (message.contains(AllowedString)) {
                return false;
            }
        }
        message = message.replace("ß", "b");
        message = removeAccents(message);
        message = message.toLowerCase();
        message = message.replaceAll("[^a-zA-Z]+", "");
        message = message.replaceAll("(\\w)\\1+", "$1");

        for (String BlockedString : blacklistedWords) {
            if (message.contains(BlockedString)) {
                return true;
            }
        }

        return false;
    }

    public String getFilteredString(String message) {
        message = message.replace("ß", "b");
        message = removeAccents(message);
        message = message.toLowerCase();
        message = message.replaceAll("[^a-zA-Z]+", "");
        message = message.replaceAll("(\\w)\\1+", "$1");
        return message;
    }

    public void removeblacklistedWord(String word) {
        blacklistedWords.remove(getFilteredString(word));
        try {
            PreparedStatement statement = Comet.getServer().getStorage().prepare("DELETE FROM wordfilter WHERE word = ? AND type = ?");

            statement.setString(1, getFilteredString(word));
            statement.setString(2, "blacklist");
            statement.execute();
        } catch (SQLException e) {
            GameEngine.getLogger().error("Error while unblacklisting word: " + word, e);
        }


    }

    public void removewhitelistedWord(String word) {
        whitelistedWords.remove(getFilteredString(word));
        try {
            PreparedStatement statement = Comet.getServer().getStorage().prepare("DELETE FROM wordfilter WHERE word = ? AND type = ?");

            statement.setString(1, getFilteredString(word));
            statement.setString(2, "whitelist");
            statement.execute();
        } catch (SQLException e) {
            GameEngine.getLogger().error("Error while unwhitelisting word: " + word, e);
        }
    }

    public void addwhitelistedWord(String word) {
        whitelistedWords.add(getFilteredString(word));
        try {
            PreparedStatement statement = Comet.getServer().getStorage().prepare("INSERT into wordfilter (`word`, `type`) VALUES(?, ?);");

            statement.setString(1, getFilteredString(word));
            statement.setString(2, "whitelist");
            statement.execute();
        } catch (SQLException e) {
            GameEngine.getLogger().error("Error while whitelisting word: " + word, e);
        }
    }

    public void addblacklistedWord(String word) {
        blacklistedWords.add(getFilteredString(word));
        try {
            PreparedStatement statement = Comet.getServer().getStorage().prepare("INSERT into wordfilter (`word`, `type`) VALUES(?, ?);");

            statement.setString(1, getFilteredString(word));
            statement.setString(2, "blacklist");
            statement.execute();
        } catch (SQLException e) {
            GameEngine.getLogger().error("Error while blacklisting word: " + word, e);
        }
    }

}