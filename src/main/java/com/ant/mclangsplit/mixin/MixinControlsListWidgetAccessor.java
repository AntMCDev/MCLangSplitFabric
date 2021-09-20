package com.ant.mclangsplit.mixin;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ControlsListWidget.class)
public interface MixinControlsListWidgetAccessor {
    @Final
    @Accessor
    ControlsOptionsScreen getParent();

    @Accessor
    int getMaxKeyNameLength();
}
