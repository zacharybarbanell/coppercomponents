package com.zacharybarbanell.coppercomponents.conductivewater;

import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="coppercomponents")
public class WaterPowerAttacher {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static class WaterPowerProvider implements ICapabilitySerializable<ListTag> {

        private final WaterPower backend = new WaterPower();

        public static final ResourceLocation IDENTIFIER = new ResourceLocation("coppercomponents", "waterpower");

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ListTag serializeNBT() {
            ListTag tag = new ListTag();
            for (Entry<BlockPos, Integer> entry : backend.getData().entrySet()) {
                CompoundTag val = new CompoundTag();
                val.putLong("key", entry.getKey().asLong());
                val.putInt("val", entry.getValue());
                tag.add(val);
            }
            return tag;
        }

        @Override
        public void deserializeNBT(ListTag nbt) {
            backend.clear();
            for (Tag tag : nbt) {
                CompoundTag entry = (CompoundTag) tag;
                BlockPos key = BlockPos.of(entry.getLong("key"));
                int val = entry.getInt("val");
                backend.setPower(key, val);
            }
        }

    }

    @SubscribeEvent
    public static void attach(final AttachCapabilitiesEvent<LevelChunk> event) {
        final WaterPowerProvider provider = new WaterPowerProvider();

        event.addCapability(WaterPowerProvider.IDENTIFIER, provider);

        LOGGER.debug("WaterPowerAttacher attach called");
    }

}