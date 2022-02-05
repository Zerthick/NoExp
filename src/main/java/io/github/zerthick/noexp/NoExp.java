/*
 * Copyright (C) 2018-2022 Zerthick
 *
 * This file is part of NoExp.
 *
 * NoExp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoExp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoSleep.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zerthick.noexp;

import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ContainerType;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Plugin(
        "noexp"
)
public class NoExp {

    private final PluginContainer instance;
    private final Logger logger;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path configPath;
    // Exp generation flags
    private Map<String, Boolean> entityMap;
    private boolean expBottle;
    private boolean fishing;
    private boolean smelting;
    private boolean disenchanting;
    private boolean trading;
    private boolean mining;

    @Inject
    NoExp(final PluginContainer instance, final Logger logger) {
        this.instance = instance;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {

        // Log Start Up to Console
        logger.info(
                instance.metadata().name().orElse("Unknown Plugin") + " version " + instance.metadata().version()
                        + " enabled!");

        // Load permission text from config
        Optional<ConfigurationNode> configOptional = loadConfig();

        if (configOptional.isPresent()) {
            try {
                ConfigurationNode config = configOptional.get();
                entityMap = config.node("entityMap").get(new TypeToken<Map<String, Boolean>>() {
                });
                expBottle = config.node("expBottle").getBoolean();
                fishing = config.node("fishing").getBoolean();
                smelting = config.node("smelting").getBoolean();
                disenchanting = config.node("disenchanting").getBoolean();
                trading = config.node("trading").getBoolean();
                mining = config.node("mining").getBoolean();
            } catch (SerializationException e) {
                logger.error("Error loading config! Error: " + e.getMessage());
            }
        } else {
            logger.error("Unable to load configuration file!");
        }
    }

    @Listener
    public void onSpawnEntity(SpawnEntityEvent event, @Getter("entities") List<Entity> entities) {

        boolean hasExpOrb = entities.stream()
                .map(Entity::type).anyMatch(e -> e.equals(EntityTypes.EXPERIENCE_ORB.get()));

        if (hasExpOrb) {
            Object causeRoot = event.cause().root();

            // Killing entities
            if (causeRoot instanceof HarvestEntityEvent) {
                HarvestEntityEvent harvestEntityEvent = (HarvestEntityEvent) causeRoot;

                Entity killedEnity = harvestEntityEvent.entity();
                boolean entityFlag = entityMap.getOrDefault(killedEnity.type().key(RegistryTypes.ENTITY_TYPE).asString(), true);

                if (!entityFlag) {
                    filterSpawnEvent(event);
                }
            } else if (causeRoot instanceof ClickContainerEvent) {
                ClickContainerEvent clickContainerEvent = (ClickContainerEvent) causeRoot;
                ContainerType containerType = clickContainerEvent.inventory().type();

                // Smelting
                if (containerType.equals(ContainerTypes.FURNACE.get()) ||
                        containerType.equals(ContainerTypes.BLAST_FURNACE.get()) ||
                        containerType.equals(ContainerTypes.SMOKER.get())) {
                    if (!smelting) {
                        filterSpawnEvent(event);
                    }
                }

                // Disenchanting
                if (containerType.equals(ContainerTypes.GRINDSTONE.get())) {
                    if (!disenchanting) {
                        filterSpawnEvent(event);
                    }
                }

                // Trading
                if (containerType.equals(ContainerTypes.MERCHANT.get())) {
                    if (!trading) {
                        filterSpawnEvent(event);
                    }
                }


            } else if (causeRoot instanceof Entity) {
                Entity entity = (Entity) causeRoot;

                if (entity instanceof ServerPlayer) {

                    ServerPlayer player = (ServerPlayer) entity;
                    EventContext context = event.context();

                    // Fishing
                    context.get(EventContextKeys.USED_ITEM).ifPresent(itemStackSnapshot -> {
                        if (itemStackSnapshot.createStack().type().equals(ItemTypes.FISHING_ROD.get())) {

                            if (!fishing) {
                                filterSpawnEvent(event);
                            }
                        }
                    });

                } else if (entity instanceof Living) { //Breeding Entities

                    boolean entityFlag = entityMap.getOrDefault(entity.type().key(RegistryTypes.ENTITY_TYPE).asString(), true);

                    if (!entityFlag) {
                        filterSpawnEvent(event);
                    }
                } else if (entity.type().equals(EntityTypes.EXPERIENCE_BOTTLE.get())) { //EXP Bottle

                    if (!expBottle) {
                        filterSpawnEvent(event);
                    }
                }
            } else if (causeRoot instanceof BlockSnapshot) {
                if (!mining) {
                    filterSpawnEvent(event);
                }
            }

        }

//        if (hasExpOrb) {
//
//            Object causeRoot = event.getCause().root();
//
//            if (causeRoot instanceof Entity) { // ExpBottle, Fishing
//                Entity entity = (Entity) causeRoot;
//                if (entity.getType().equals(EntityTypes.THROWN_EXP_BOTTLE)) { // ExpBottle
//                    if (!expBottle) {
//                        filterSpawnEvent(event);
//                    }
//                } else if (entity.getType().equals(EntityTypes.PLAYER)) {
//                    EventContext eventContext = event.getContext();
//
//                    eventContext.get(EventContextKeys.SPAWN_TYPE).ifPresent(spawnType -> {
//
//                        if (spawnType.equals(SpawnTypes.PLACEMENT)) { // Fishing
//                            eventContext.get(EventContextKeys.USED_ITEM).ifPresent(itemStackSnapshot -> {
//
//                                if (itemStackSnapshot.getType().equals(ItemTypes.FISHING_ROD)) {
//                                    if (!fishing) {
//                                        filterSpawnEvent(event);
//                                    }
//                                }
//
//                            });
//                        }
//                    });
//
//                } else {
//
//                    boolean entityFlag = entityMap.getOrDefault(entity.getType().getId(), true);
//
//                    if (!entityFlag) { // Entities Death % Breeding
//                        filterSpawnEvent(event);
//                    }
//
//                }
//            } else if (causeRoot instanceof Inventory) { // Smelting
//                Inventory inventory = (Inventory) causeRoot;
//
//                if (inventory.getArchetype().equals(InventoryArchetypes.FURNACE)) {
//                    if (!smelting) {
//                        filterSpawnEvent(event);
//                    }
//                }
//            } else if (causeRoot instanceof BlockSnapshot) { // Mining
//                if (!mining) {
//                    filterSpawnEvent(event);
//                }
//            }
//        }

    }

    private Optional<ConfigurationNode> loadConfig() {
        if (!configPath.toFile().exists()) {
            // Create config if not exists
            instance.openResource(URI.create("default.conf")).ifPresent(r -> {
                try {
                    Files.copy(r, configPath);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });
        }
        try {
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().path(configPath).build();
            return Optional.of(loader.load());
        } catch (ConfigurateException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private void filterSpawnEvent(SpawnEntityEvent event) {
        event.filterEntities(e -> !e.type().equals(EntityTypes.EXPERIENCE_ORB.get()));
    }

}
