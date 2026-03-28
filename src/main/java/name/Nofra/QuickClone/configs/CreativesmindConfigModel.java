package name.Nofra.QuickClone.configs;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = "QuickClone")
@Config(name = "QuickClone-config", wrapperName = "QuickCloneConfig")
public class CreativesmindConfigModel {

    @SectionHeader("world")
    public boolean deleteSaveWhenLeaving = true;
    public boolean redirectToOldSave = true;
    public boolean brandNewWorld = false;

    @SectionHeader("player")
    public boolean giveOperator = true;
    public boolean confirmationPrompt = true;
    @RangeConstraint(min = 0, max = 3)
    public int gameMode = 1;
}