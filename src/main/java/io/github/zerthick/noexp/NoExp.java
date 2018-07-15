/*
 * Copyright (C) 2018  Zerthick
 *
 * This file is part of NoExp.
 *
 * NoExp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * NoExp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoExp.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.noexp;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Plugin(
        id = "noexp",
        name = "NoExp",
        description = "A simple Minecraft experience control plugin.",
        authors = {
                "Zerthick"
        }
)
public class NoExp {

    @Inject
    private Logger logger;
    @Inject
    private PluginContainer instance;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    // Exp generation flags
    private Map<String, Boolean> entityMap;
    private boolean expBottle;
    private boolean fishing;
    private boolean smelting;
    private boolean mining;

    @Listener
    public void onGameInit(GameInitializationEvent event) {

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

        // Generate default config if it doesn't exist
        if (!defaultConfig.toFile().exists()) {
            Asset defaultConfigAsset = getInstance().getAsset("DefaultConfig.conf").get();
            try {
                defaultConfigAsset.copyToFile(defaultConfig);
                configLoader.save(configLoader.load());
            } catch (IOException e) {
                logger.warn("Error loading default config! Error: " + e.getMessage());
            }
        }

        // Load config values
        try {
            CommentedConfigurationNode configNode = configLoader.load();
            entityMap = configNode.getNode("entityMap").getValue(new TypeToken<Map<String, Boolean>>() {});
            expBottle = configNode.getNode("expBottle").getBoolean();
            fishing = configNode.getNode("fishing").getBoolean();
            smelting = configNode.getNode("smelting").getBoolean();
            mining = configNode.getNode("mining").getBoolean();
        } catch (ObjectMappingException | IOException e) {
            logger.error("Error loading config! Error: " + e.getMessage());
        }

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getLogger().info(
                instance.getName() + " version " + instance.getVersion().orElse("")
                        + " enabled!");
    }

    @Listener
    public void onSpawnEntity(SpawnEntityEvent event, @Getter("getEntities") List<Entity> entities) {

        boolean hasExpOrb = !entities.stream()
                .map(Entity::getType)
                .filter(e -> e.equals(EntityTypes.EXPERIENCE_ORB))
                .collect(Collectors.toList()).isEmpty();

        if(hasExpOrb) {

            Object causeRoot = event.getCause().root();

            if (causeRoot instanceof Entity) { // ExpBottle, Fishing
                Entity entity = (Entity) causeRoot;
                if (entity.getType().equals(EntityTypes.THROWN_EXP_BOTTLE)) { // ExpBottle
                    if(!expBottle) {
                        filterSpawnEvent(event);
                    }
                } else if (entity.getType().equals(EntityTypes.PLAYER)) {
                    EventContext eventContext = event.getContext();

                    eventContext.get(EventContextKeys.SPAWN_TYPE).ifPresent(spawnType -> {

                        if(spawnType.equals(SpawnTypes.PLACEMENT)) { // Fishing
                            eventContext.get(EventContextKeys.USED_ITEM).ifPresent(itemStackSnapshot -> {

                                if(itemStackSnapshot.getType().equals(ItemTypes.FISHING_ROD)) {
                                    if(!fishing) {
                                        filterSpawnEvent(event);
                                    }
                                }

                            });
                        }
                    });

                } else {

                    boolean entityFlag = entityMap.getOrDefault(entity.getType().getId(), true);

                    if(!entityFlag) { // Entities Death % Breeding
                        filterSpawnEvent(event);
                    }

                }
            } else if(causeRoot instanceof Inventory) { // Smelting
                Inventory inventory = (Inventory)causeRoot;

                if(inventory.getArchetype().equals(InventoryArchetypes.FURNACE)) {
                    if(!smelting) {
                        filterSpawnEvent(event);
                    }
                }
            } else if (causeRoot instanceof BlockSnapshot) { // Mining
                if (!mining) {
                    filterSpawnEvent(event);
                }
            }
        }

    }

    public Logger getLogger() {
        return logger;
    }

    public PluginContainer getInstance() {
        return instance;
    }

    private void filterSpawnEvent(SpawnEntityEvent event) {
        event.filterEntities(e -> !e.getType().equals(EntityTypes.EXPERIENCE_ORB));
    }

}
