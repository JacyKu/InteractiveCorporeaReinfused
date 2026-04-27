package shblock.interactivecorporea.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.level.Level;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;

public class NBTTagHelper {
  @Nullable
  public static GlobalPos getGlobalPos(Tag nbt) {
    if (nbt == null)
      return null;
    Pair<GlobalPos, Tag> result = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt).result().orElse(null);
    if (result == null)
      return null;
    return result.getFirst();
  }

  @Nullable
  public static Tag putGlobalPos(GlobalPos pos) {
    if (pos == null)
      return null;
    return GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).get().orThrow();
  }

  public static Vector3f getVector3f(CompoundTag nbt) {
    return new Vector3f(
        nbt.getFloat("x"),
        nbt.getFloat("y"),
        nbt.getFloat("z")
    );
  }

  public static CompoundTag putVector3f(Vector3f vec) {
    CompoundTag nbt = new CompoundTag();
    nbt.putFloat("x", vec.getX());
    nbt.putFloat("y", vec.getY());
    nbt.putFloat("z", vec.getZ());
    return nbt;
  }

  public static Vector3d getVector3d(CompoundTag nbt) {
    return new Vector3d(
        nbt.getDouble("x"),
        nbt.getDouble("y"),
        nbt.getDouble("z")
    );
  }

  public static CompoundTag putVector3d(Vector3d vec) {
    CompoundTag nbt = new CompoundTag();
    nbt.putDouble("x", vec.getX());
    nbt.putDouble("y", vec.getY());
    nbt.putDouble("z", vec.getZ());
    return nbt;
  }

  public static Vector3 getVector3(CompoundTag nbt) {
    return new Vector3(
        nbt.getDouble("x"),
        nbt.getDouble("y"),
        nbt.getDouble("z")
    );
  }

  public static CompoundTag putVector3(Vector3 vec) {
    CompoundTag nbt = new CompoundTag();
    nbt.putDouble("x", vec.x);
    nbt.putDouble("y", vec.y);
    nbt.putDouble("z", vec.z);
    return nbt;
  }

  public static Level getWorld(CompoundTag nbt, String tag) {
    Pair<ResourceKey<Level>, Tag> result = Level.RESOURCE_KEY_CODEC.decode(NbtOps.INSTANCE, nbt.get(tag)).result().orElse(null);
    if (result == null) return null;
    ResourceKey<Level> reg = result.getFirst();
    return WorldHelper.getWorldFromName(reg);
  }

  public static void putWorld(CompoundTag nbt, String tag, Level world) {
    nbt.put(tag, Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, world.dimension()).get().orThrow());
  }

  public static CompoundTag putCurioSlot(CurioSlotPointer slot) {
    CompoundTag nbt = new CompoundTag();
    nbt.putString("identifier", slot.identifier);
    nbt.putInt("slot", slot.slot);
    return nbt;
  }

  public static CurioSlotPointer getCurioSlot(CompoundTag nbt) {
    return new CurioSlotPointer(nbt.getString("identifier"), nbt.getInt("slot"));
  }

  public static CompoundTag putCISlot(CISlotPointer slot) {
    CompoundTag nbt = new CompoundTag();
    nbt.putBoolean("is_inv", slot.isInventory());
    if (slot.isInventory()) {
      nbt.putInt("slot", slot.getInventory());
    } else {
      nbt.put("slot", putCurioSlot(slot.getCurio()));
    }
    return nbt;
  }

  public static CISlotPointer getCISlot(CompoundTag nbt) {
    if (nbt.getBoolean("is_inv")) {
      return new CISlotPointer(nbt.getInt("slot"));
    } else {
      return new CISlotPointer(getCurioSlot(nbt.getCompound("slot")));
    }
  }
}
