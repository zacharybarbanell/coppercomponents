package com.zacharybarbanell.coppercomponents.conductivewater;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="coppercomponents")
public class WaterPower {

    public static final Capability<WaterPower> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    private final Map<BlockPos,Integer> power;

    private static final Logger LOGGER = LogUtils.getLogger();

    public WaterPower() {
        power = new HashMap<BlockPos,Integer>();
    }

    public Map<BlockPos,Integer> getData() {
        return power;
    }

    public int getPower(BlockPos pos) {
        if(power.containsKey(pos)){
            return power.get(pos);
        }
        else{
            return 0;
        }
    }

    public void setPower(BlockPos pos, int n) {
        power.put(pos, n);
    }

    public void clear(){
        power.clear();
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(WaterPower.class);
        LOGGER.debug("WaterPower register called");
    }
    
}
