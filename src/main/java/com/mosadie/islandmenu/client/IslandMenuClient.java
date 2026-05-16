package com.mosadie.islandmenu.client;

import com.mosadie.islandmenu.client.theme.HalloweenTheme;
import com.mosadie.islandmenu.client.theme.NormalTheme;
import com.mosadie.islandmenu.client.theme.WinterTheme;
import com.mosadie.islandmenu.mccapi.EventInfo;
import com.mosadie.islandmenu.mccapi.MCCApi;
import com.mosadie.islandmenu.mccapi.ParticipantsInfo;
import com.mosadie.islandmenu.mccapi.Teams;
import com.mosadie.simplemainmenu.api.SplashText;
import com.mosadie.simplemainmenu.client.SimpleMainMenuLibClient;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class IslandMenuClient implements ClientModInitializer {

    public static final String MOD_ID = "island-menu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MCCApi mccApi;

    private static NormalTheme normalTheme = new NormalTheme();
    private static HalloweenTheme halloweenTheme = new HalloweenTheme();

    private static WinterTheme winterTheme = new WinterTheme();

    private final static String MCC_DATE_SPLASH = "island-menu.mccdatesplash";
    private final static String MCC_PLAYER_SPLASH = "island-menu.playersplash";

    private final static String MCC_TEAM_SPLASH = "island-menu.teamsplash";

    private final static String[] splashOptions = {
            "Set Sail Today!",
            "Enjoy your island vacation!",
            "Get the latest news at mccisland.net!",
            "See live MCC stats at mcc.live!",
            "Support the Noxcrew!",
            MCC_TEAM_SPLASH,
            MCC_DATE_SPLASH,
            MCC_PLAYER_SPLASH

    };

    private final static String[] teamSplashOptions = {
            "Go %s!",
            "Support the %s!"
    };

    public static SplashText getSplashText() {
        String splash =  splashOptions[RandomSource.create().nextInt(0, splashOptions.length)];
        SplashText.Builder splashTextBuilder = SplashText.builder();

        switch(splash) {
            case MCC_DATE_SPLASH:
                if (mccApi != null) {
                    EventInfo eventInfo = mccApi.getEventInfo();
                    if (eventInfo != null) {
                        if (eventInfo.getData().getDate().after(new Date())) {
                            String dateString = new SimpleDateFormat("MMMM dd 'at' h:mm a z").format(eventInfo.getData().getDate());
                            splashTextBuilder.addLine(String.format("Watch MCC %s on", eventInfo.getData().getEvent()));
                            splashTextBuilder.addLine(String.format("%s!", dateString));
                        } else {
                            splashTextBuilder.addLine(String.format("What did you think of MCC %s?", eventInfo.getData().getEvent()));
                        }
                    }
                }
                return splashTextBuilder.build();

            case MCC_PLAYER_SPLASH:
                if (mccApi != null) {
                    ParticipantsInfo participantsInfo = mccApi.getParticipantsInfo();

                    if (participantsInfo != null) {
                        ParticipantsInfo.ParticipantsData.Participant player = null;

                        ParticipantsInfo.ParticipantsData.Participant selfPlayer = participantsInfo.getPlayer(Minecraft.getInstance().getGameProfile().id());
                        if (selfPlayer != null) {
                            LOGGER.debug("MCC Player found! Picking them always for splash text.");
                            player = selfPlayer;
                        } else {
                            List<Teams> teams = new java.util.ArrayList<>(List.of(Teams.values().clone()));
                            Collections.shuffle(teams);

                            for (Teams team : teams) {
                                if (team.equals(Teams.NONE) || team.equals(Teams.SPECTATORS)) {
                                    continue;
                                }

                                ParticipantsInfo.ParticipantsData.Participant[] members = participantsInfo.getData().getTeam(team);

                                if (members.length != 0) {
                                    int memberIndex = RandomUtils.nextInt(0, members.length);
                                    player = members[memberIndex];
                                    break;
                                }
                            }

                            if (player == null) {
                                splashTextBuilder.addLine(splashOptions[0]);
                                return splashTextBuilder.build();
                            }
                        }

                        if (player == null || player.getUsername() == null) {
                            splashTextBuilder.addLine(splashOptions[0]);
                            return splashTextBuilder.build();
                        }

                        splashTextBuilder.addLine(String.format("Check out %s", player.getUsername()));

                        EventInfo eventInfo = mccApi.getEventInfo();
                        if (eventInfo != null && eventInfo.getData().getDate().after(new Date())) {
                            splashTextBuilder.addLine(String.format("in MCC %s!", eventInfo.getData().getEvent()));
                        }

                        return splashTextBuilder.build();
                    }
                }

                splashTextBuilder.addLine(splashOptions[0]);
                return splashTextBuilder.build();

            case MCC_TEAM_SPLASH:
                Teams team = null;

                if (config != null) {
                    team = config.supportingTeam;
                }

                // If player is in the next/most recent MCC, pick their team.
                if ((team == null || team.equals(Teams.NONE)) && mccApi != null && mccApi.getParticipantsInfo() != null && Minecraft.getInstance().getGameProfile().id() != null) {
                    Teams selfTeam = mccApi.getParticipantsInfo().getPlayerTeam(Minecraft.getInstance().getGameProfile().id());
                    if (!selfTeam.equals(Teams.NONE)) {
                        LOGGER.debug("MCC Player team found!");
                        team = selfTeam;
                    }
                }

                while(team == null || team.equals(Teams.NONE) || (team.equals(Teams.SPECTATORS) && !config.supportingTeam.equals(Teams.SPECTATORS))) {
                    team = Teams.values()[RandomUtils.nextInt(0, Teams.values().length)];
                }

                String teamString = teamSplashOptions[RandomUtils.nextInt(0, teamSplashOptions.length)];

                splashTextBuilder.addLine(String.format(teamString, team.getName()));
                return splashTextBuilder.build();

            default:
                return splashTextBuilder.addLine(splash).build();
        }
    }

    private static IslandMenuConfig config;
    private static final Identifier MCCI_JOIN_LOGO = Identifier.fromNamespaceAndPath(MOD_ID, "textures/joingui/mccisland_joinlogo.png");
    private static final int MCCI_JOIN_LOGO_TEXTURE_WIDTH = 4000;
    private static final int MCCI_JOIN_LOGO_TEXTURE_HEIGHT = 1746;
    private static boolean joiningMcciServer;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Island Menu...");

        LOGGER.info("Registering Theme...");

        Registry.register(SimpleMainMenuLibClient.registry, Identifier.fromNamespaceAndPath(IslandMenuClient.MOD_ID, "normal"), normalTheme);
        Registry.register(SimpleMainMenuLibClient.registry, Identifier.fromNamespaceAndPath(IslandMenuClient.MOD_ID, "halloween"), halloweenTheme);
        Registry.register(SimpleMainMenuLibClient.registry, Identifier.fromNamespaceAndPath(IslandMenuClient.MOD_ID, "winter"), winterTheme);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> joiningMcciServer = false);

        LOGGER.info("Configuring Config...");

        AutoConfig.register(IslandMenuConfig.class, GsonConfigSerializer::new);

        AutoConfig.getConfigHolder(IslandMenuConfig.class).registerSaveListener(IslandMenuClient::onConfigSave);

        config = AutoConfig.getConfigHolder(IslandMenuConfig.class).getConfig();

        LOGGER.info("Requesting information from MCC API...");

        mccApi = new MCCApi(config.devOptions.apiUrl);

        // Calls to make the cache when minor lag is fine.
        mccApi.getEventInfo();
        mccApi.getParticipantsInfo();


        LOGGER.info("Island Menu Initialized!");
    }

    public static void setJoiningServerIp(String serverIp) {
        joiningMcciServer = isMcciIslandServer(serverIp);
    }

    public static void extractMcciJoinLogo(Screen screen, GuiGraphicsExtractor graphics) {
        if (!shouldShowMcciJoinLogo(Minecraft.getInstance())) {
            return;
        }

        int logoWidth = Math.min(200, Math.max(80, screen.width - 40));
        int logoHeight = Math.round(logoWidth * (MCCI_JOIN_LOGO_TEXTURE_HEIGHT / (float) MCCI_JOIN_LOGO_TEXTURE_WIDTH));
        int x = screen.width / 2 - logoWidth / 2;
        int y = Math.max(18, screen.height / 2 - 50 - logoHeight - 16);

        graphics.blit(RenderPipelines.GUI_TEXTURED, MCCI_JOIN_LOGO, x, y, 0.0F, 0.0F, logoWidth, logoHeight, MCCI_JOIN_LOGO_TEXTURE_WIDTH, MCCI_JOIN_LOGO_TEXTURE_HEIGHT, MCCI_JOIN_LOGO_TEXTURE_WIDTH, MCCI_JOIN_LOGO_TEXTURE_HEIGHT);
    }

    private static boolean shouldShowMcciJoinLogo(Minecraft client) {
        if (client.getCurrentServer() != null) {
            return isMcciIslandServer(client.getCurrentServer().ip);
        }

        return joiningMcciServer;
    }

    private static boolean isMcciIslandServer(String serverIp) {
        if (serverIp == null) {
            return false;
        }

        String host = serverIp.toLowerCase(Locale.ROOT).trim();
        int portStart = host.indexOf(':');
        if (portStart >= 0) {
            host = host.substring(0, portStart);
        }
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }

        return host.equals("mccisland.net") || host.endsWith(".mccisland.net");
    }

    private static InteractionResult onConfigSave(ConfigHolder<IslandMenuConfig> islandMenuConfigConfigHolder, IslandMenuConfig islandMenuConfig) {
        LOGGER.info("Updating config!");

//        config = islandMenuConfig;

        if (mccApi == null || !mccApi.getBaseUrl().equalsIgnoreCase(config.devOptions.apiUrl))
            mccApi = new MCCApi(config.devOptions.apiUrl);

        return InteractionResult.PASS;
    }
}
