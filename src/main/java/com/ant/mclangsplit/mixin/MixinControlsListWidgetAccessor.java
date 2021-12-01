package com.ant.mclangsplit.mixin;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ControlsListWidget.class)
public interface MixinControlsListWidgetAccessor {
    @Final
    @Accessor
    KeybindsScreen getParent();

    @Accessor
    int getMaxKeyNameLength();
}
