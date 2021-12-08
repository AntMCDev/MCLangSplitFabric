package com.ant.mclangsplit.mixin;

import net.minecraft.client.gui.screen.options.ControlsListWidget;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
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
