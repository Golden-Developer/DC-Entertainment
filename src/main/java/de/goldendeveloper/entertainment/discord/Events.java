package de.goldendeveloper.entertainment.discord;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.goldendeveloper.entertainment.Main;
import de.goldendeveloper.entertainment.discord.music.GuildMusicManager;
import de.goldendeveloper.mysql.entities.Column;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.Row;
import de.goldendeveloper.mysql.entities.Table;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Events extends ListenerAdapter {

    public static final String movie = "movie";
    public static final String serien = "serien";
    public static final String jokes = "jokes";
    public static final String games = "games";
    public static final String eightBall = "eight-ball";
    public static final String fact = "facts";
    public static final String skip = "skip";
    public static final String firstLetter = "firstLetter";

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public Events() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent e) {
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
        embed.setAuthor(new WebhookEmbed.EmbedAuthor(Main.getDiscord().getBot().getSelfUser().getName(), Main.getDiscord().getBot().getSelfUser().getAvatarUrl(), "https://Golden-Developer.de"));
        embed.addField(new WebhookEmbed.EmbedField(false, "[Status]", "OFFLINE"));
        embed.setColor(0xFF0000);
        embed.setFooter(new WebhookEmbed.EmbedFooter("@Golden-Developer", Main.getDiscord().getBot().getSelfUser().getAvatarUrl()));
        new WebhookClientBuilder(Main.getConfig().getDiscordWebhook()).build().send(embed.build());

        System.exit(0);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        String cmd = e.getName();
        User _Coho04_ = e.getJDA().getUserById("513306244371447828");
        User zRazzer = e.getJDA().getUserById("428811057700536331");
        if (cmd.equalsIgnoreCase(Discord.cmdEntertainment)) {
            MessageEmbed embed = new EmbedBuilder().setTitle("Wähle aus welches Entertainment Programm du haben möchtest!").build();
            e.getInteraction().replyEmbeds(embed).addActionRow(
                    Button.danger(movie, "Filme"),
                    Button.primary(serien, "Serien"),
                    Button.secondary(jokes, "Jokes"),
                    Button.success(games, "Games"),
                    Button.primary(fact, "Fakten")
            ).addActionRow(
                    Button.secondary(eightBall, "Eight-Ball"),
                    Button.link("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "P*rno")
            ).queue();
        } else if (cmd.equalsIgnoreCase(Discord.cmdEmojiStart)) {
            if (e.isFromGuild()) {
                if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    if (Main.getMysql().existsDatabase(Main.dbName)) {
                        Database db = Main.getMysql().getDatabase(Main.dbName);
                        if (db.existsTable(Main.DiscordTable)) {
                            Table table = db.getTable(Main.DiscordTable);
                            if (table.existsColumn(Main.DiscordID)) {
                                Column column = table.getColumn(Main.DiscordID);
                                if (column.getAll().contains(e.getGuild().getId())) {
                                    HashMap<String, Object> map = table.getRow(table.getColumn(Main.DiscordID), e.getGuild().getId());
                                    if (map.containsKey(Main.emojiGameChannelID)) {
                                        String Channel = map.get(Main.emojiGameChannelID).toString();
                                        if (!Channel.isEmpty() && !Channel.isBlank()) {
                                            TextChannel channel = e.getGuild().getTextChannelById(Channel);
                                            if (channel != null) {
                                                channel.sendMessageEmbeds(Objects.requireNonNull(EmojiEmbed())).setActionRows(
                                                        ActionRow.of(
                                                                Button.danger(skip, "Überspringen"),
                                                                Button.primary(firstLetter, "Erster Buchstabe")
                                                        )
                                                ).queue();
                                            }
                                        }
                                    }
                                } else {
                                    Guild guild = e.getGuild();
                                    guild.createTextChannel("emoji-quiz").queue(channel -> {
                                        table.insert(new Row(table, table.getDatabase())
                                                .with(Main.DiscordID, e.getGuild().getId())
                                                .with(Main.emojiGameChannelID, channel.getId())
                                        );
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase(Discord.cmdGalgenStart)) {
            if (e.isFromGuild()) {
                if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    if (Main.getMysql().existsDatabase(Main.dbName)) {
                        Database db = Main.getMysql().getDatabase(Main.dbName);
                        if (db.existsTable(Main.DiscordTable)) {
                            Table table = db.getTable(Main.DiscordTable);
                            if (table.existsColumn(Main.DiscordID)) {
                                Column column = table.getColumn(Main.DiscordID);
                                if (column.getAll().contains(e.getGuild().getId())) {
                                    HashMap<String, Object> map = table.getRow(table.getColumn(Main.DiscordID), e.getGuild().getId());
                                    if (map.containsKey(Main.emojiGameChannelID)) {
                                        String Channel = map.get(Main.emojiGameChannelID).toString();
                                        if (!Channel.isEmpty() && !Channel.isBlank()) {
                                            TextChannel channel = e.getGuild().getTextChannelById(Channel);
                                            if (channel != null) {
                                                channel.sendMessageEmbeds(EmojiEmbed()).setActionRows(
                                                        ActionRow.of(
                                                                Button.danger(skip, "Überspringen"),
                                                                Button.primary(firstLetter, "Erster Buchstabe")
                                                        )
                                                ).queue();
                                            }
                                        }
                                    }
                                } else {
                                    Guild guild = e.getGuild();
                                    guild.createTextChannel("emoji-quiz").queue(channel -> {
                                        table.insert(new Row(table, table.getDatabase())
                                                .with(Main.DiscordID, e.getGuild().getId())
                                                .with(Main.emojiGameChannelID, channel.getId())
                                        );
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase(Discord.cmdHelp)) {
            List<Command> commands = Main.getDiscord().getBot().retrieveCommands().complete();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("**Help Commands**");
            embed.setColor(Color.MAGENTA);
            embed.setFooter("@Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
            for (Command cm : commands) {
                embed.addField("/" + cm.getName(), cm.getDescription(), true);
            }
            e.getInteraction().replyEmbeds(embed.build()).addActionRow(
                    Button.link("https://wiki.Golden-Developer.de/", "Online Übersicht"),
                    Button.link("https://support.Golden-Developer.de", "Support Anfragen")
            ).queue();
        } else if (e.getName().equalsIgnoreCase(Discord.getCmdShutdown)) {
            if (e.getUser() == zRazzer || e.getUser() == _Coho04_) {
                e.getInteraction().reply("Der Bot wird nun heruntergefahren").queue();
                e.getJDA().shutdown();
            } else {
                e.getInteraction().reply("Dazu hast du keine Rechte du musst für diesen Befehl der Bot inhaber sein!").queue();
            }
        } else if (e.getName().equalsIgnoreCase(Discord.getCmdRestart)) {
            if (e.getUser() == zRazzer || e.getUser() == _Coho04_) {
                try {
                    e.getInteraction().reply("Der Discord Bot wird nun neugestartet!").queue();
                    Process p = Runtime.getRuntime().exec("screen -AmdS GD-Entertainment java -Xms1096M -Xmx1096M -jar GD-Entertainment-1.0.jar");
                    p.waitFor();
                    e.getJDA().shutdown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                e.getInteraction().reply("Dazu hast du keine Rechte du musst für diesen Befehl der Bot inhaber sein!").queue();
            }
        } else if (cmd.equalsIgnoreCase(Discord.cmdPlay)) {
            String TrackUrl = e.getOption(Discord.cmdPlayOptionTrack).getAsString();
            if (e.isFromGuild()) {
                loadAndPlay(e, TrackUrl);
            } else {
                e.reply("Dieser Command ist nur auf einem Server möglich!").queue();
            }
        } else if (cmd.equalsIgnoreCase(Discord.cmdSkip)) {
            if (e.isFromGuild()) {
                skipTrack(e);
            } else {
                e.reply("Dieser Command ist nur auf einem Server möglich!").queue();
            }
        } else if (cmd.equalsIgnoreCase(Discord.cmdStop)) {
            if (e.isFromGuild()) {
                stopTrack(e);
            } else {
                e.reply("Dieser Command ist nur auf einem Server möglich!").queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        String button = e.getButton().getId();
        if (button != null) {
            switch (button) {
                case serien ->
                        e.getInteraction().reply("Wir empfehlen dir die Serie [" + getItem(serien) + "]!").queue();
                case movie -> e.getInteraction().reply("Wir empfehlen dir den Film [" + getItem(movie) + "]!").queue();
                case games -> e.getInteraction().reply("Wir empfehlen dir das Game [" + getItem(games) + "]!").queue();
                case fact -> e.getInteraction().reply(getItem(fact)).queue();
                case jokes -> e.getInteraction().reply(getItem(jokes)).queue();
                case eightBall -> e.getInteraction().reply(getItem(eightBall)).queue();
                case skip -> {
                    Message msg = e.getChannel().getHistory().getMessageById(e.getChannel().getLatestMessageId());
                    if (msg != null) {
                        msg.editMessageEmbeds(EmojiEmbed()).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.isFromGuild()) {
            if (Main.getMysql().existsDatabase(Main.dbName)) {
                Database db = Main.getMysql().getDatabase(Main.dbName);
                if (db.existsTable(Main.DiscordTable)) {
                    Table table = db.getTable(Main.DiscordTable);
                    if (table.existsColumn(Main.DiscordID)) {
                        if (table.getColumn(Main.DiscordID).getAll().contains(e.getGuild().getId())) {
                            if (table.existsColumn(Main.galgenGameChannelID)) {
                                if (table.getColumn(Main.galgenGameChannelID).getAll().contains(e.getTextChannel().getId())) {
                                    HashMap<String, Object> row = table.getRow(table.getColumn(Main.DiscordID), e.getGuild().getId());
                                    if (row.containsKey(Main.galgenGameChannelID)) {
                                        if (row.get(Main.galgenGameChannelID).toString().equalsIgnoreCase(e.getTextChannel().getId())) {
                                            TextChannel channel = e.getTextChannel();
                                            Message message = channel.getHistory().getMessageById(channel.getLatestMessageId());
                                            if (message != null) {
                                                if (!message.getEmbeds().isEmpty()) {
                                                    for (MessageEmbed embed : message.getEmbeds()) {

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*if (e.isFromGuild()) {
            if (Main.getMysql().existsDatabase(Main.dbName)) {
                Database db = Main.getMysql().getDatabase(Main.dbName);
                if (db.existsTable(Main.DiscordTable)) {
                    Table table = db.getTable(Main.DiscordTable);
                    if (table.existsColumn(Main.DiscordID)) {
                        if (table.getColumn(Main.DiscordID).getAll().contains(e.getGuild().getId())) {
                            HashMap<String, Object> map = table.getRow(table.getColumn(Main.DiscordID), e.getGuild().getId());
                            if (e.getChannel().getId().equalsIgnoreCase(map.get(Main.ChannelID).toString())) {
                                //if (e.getMessage().getContentRaw().equalsIgnoreCase("")) {
                                TextChannel channel = e.getTextChannel();
                                Message message = channel.getHistory().getMessageById(channel.getLatestMessageId());
                                e.getMessage().delete().queue();
                                if (message != null) {
                                    message.editMessageEmbeds(EmojiEmbed()).queue();
                                }
                                //}
                            }
                        }
                    }
                }
            }
        }*/
    }

    private void loadAndPlay(final SlashCommandInteractionEvent e, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(e.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                e.reply(track.getInfo().title + " wurde der Warteschlange hinzugefügt!").queue();
                play(e.getGuild(),e.getMember(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                e.reply(firstTrack.getInfo().title + " wurde der Warteschlange hinzugefügt! (Erster Song der Playlist: " + playlist.getName() + ")").queue();
                play(e.getGuild(),e.getMember(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                e.reply("Es konnte nichts gefunden werden mit dem Link: " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                e.reply(exception.getMessage() + " konnte nicht abgespielt werden!").queue();
            }
        });
    }

    private void play(Guild guild, Member member, GuildMusicManager musicManager, AudioTrack track) {
        connectToVoiceChannel(member, guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    private static void connectToVoiceChannel(Member member, AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            if (member.getVoiceState().inAudioChannel()) {
                audioManager.openAudioConnection(member.getVoiceState().getChannel());
            }
        }
    }

    private void skipTrack(SlashCommandInteractionEvent e) {
        GuildMusicManager musicManager = getGuildAudioPlayer(e.getGuild());
        musicManager.scheduler.nextTrack();
        e.reply("Der nächste Song wird abgespielt!").queue();
    }

    private void stopTrack(SlashCommandInteractionEvent e) {
        GuildMusicManager musicManager = getGuildAudioPlayer(e.getGuild());
        if (musicManager.getPlayer().getPlayingTrack() != null) {
            musicManager.getPlayer().stopTrack();
            if (e.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                e.getGuild().getAudioManager().closeAudioConnection();
                e.reply("Ich beende die Vorstellung!").queue();
            }
        } else {
            e.reply("Es wird momentan nichts abgespielt!").queue();
        }
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = musicManagers.get(guild.getIdLong());
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guild.getIdLong(), musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    public static String getItem(String ID) {
        Table table = null;
        switch (ID) {
            case movie -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.movieTName);
            case serien -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.serienTName);
            case games -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.gameTName);
            case jokes -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.jokeTName);
            case fact -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.factTName);
            case eightBall -> table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.eightBallTName);
        }
        if (table != null) {
            return table.getRandomFromColumn(Main.columnName).toString();
        }
        return "";
    }

    public static MessageEmbed EmojiEmbed() {
        if (Main.getMysql().existsDatabase(Main.dbName)) {
            Database db = Main.getMysql().getDatabase(Main.dbName);
            if (db.existsTable(Main.GameTable)) {
                Table table = db.getTable(Main.GameTable);
                if (table.existsColumn("id")) {
                    Column id = table.getColumn("id");
                    HashMap<String, Object> row = table.getRow(id, Integer.toString(new Random().nextInt(id.getAll().size())));
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Emoji Quiz");
                    builder.addField("", "Gesuchter Begriff: " + row.get(Main.GameEmojiOne).toString() + " " + row.get(Main.GameEmojiTwo).toString(), true);
                    builder.addField("Schwierigkeit: ", row.get(Main.GameDifficulty).toString(), true);
                    builder.addField("Tipp: ", row.get(Main.GameHint).toString(), true);
                    builder.setFooter("» Dir fällt der Begriff nicht ein? Nutze den Überspringen-Button, um das Quiz zu überspringen.");
                    return builder.build();
                }
            }
        }
        return null;
    }
}