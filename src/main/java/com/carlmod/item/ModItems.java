package com.carlmod.item;

import com.carlmod.CarlMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item BIG_MOUTH_STAFF = registerItem("big_mouth_staff",
            new BigMouthStaffItem(new Item.Settings().maxCount(1)));

    public static final RegistryKey<ItemGroup> CARL_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, new Identifier(CarlMod.MOD_ID, "carl_group"));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(CarlMod.MOD_ID, name), item);
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, CARL_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(BIG_MOUTH_STAFF))
                        .displayName(Text.translatable("itemGroup.carlmod.carl_group"))
                        .entries((displayContext, entries) -> entries.add(BIG_MOUTH_STAFF))
                        .build());

        CarlMod.LOGGER.info("[CarlMod] Items registered.");
    }
}
