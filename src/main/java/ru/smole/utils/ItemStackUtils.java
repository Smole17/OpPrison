package ru.smole.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.xfenilafs.core.util.ItemUtil;

import java.util.Map;

@UtilityClass
public class ItemStackUtils {
    private final static ReflectUtil.FieldAccessor<Map<String, NBTBase>> acc = ReflectUtil.fieldAccessor(ReflectUtil.findField(NBTTagCompound.class, Map.class));

    public static boolean hasName(ItemStack itemStack, String name) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equals(name);
    }

    public static ItemUtil.ItemBuilder setTag(ItemUtil.ItemBuilder itemBuilder, String tag, NBTBase value) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        nmsTag.set(tag, value);
        nms.setTag(nmsTag);
        return new ItemUtil.ItemBuilder(CraftItemStack.asBukkitCopy(nms));
    }

    public static <T extends NBTBase> T getTag(ItemUtil.ItemBuilder itemBuilder, String tag) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        return nmsTag.get(tag) == null ? null : (T) nmsTag.get(tag);
    }

    public static Map<String, NBTBase> getTags(ItemUtil.ItemBuilder itemBuilder) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        return acc.get(nmsTag);
    }

    public static ItemUtil.ItemBuilder setTags(ItemUtil.ItemBuilder itemBuilder, Map<String, NBTBase> tags) {
        net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemBuilder.build());
        NBTTagCompound nmsTag = nms.getTag();
        if (nmsTag == null)
            nmsTag = new NBTTagCompound();
        for (String key : tags.keySet()) {
            nmsTag.set(key, tags.get(key));
        }
        return new ItemUtil.ItemBuilder(CraftItemStack.asBukkitCopy(nms));
    }

    public static String convertItemStackToJsonRegular(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
        nmsItemStack.save(compound);

        return compound.toString();
    }
}
