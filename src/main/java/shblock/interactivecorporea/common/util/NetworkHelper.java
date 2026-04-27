package shblock.interactivecorporea.common.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import vazkii.botania.common.core.helper.Vector3;

public class NetworkHelper {
  public static void writeCurioSlotPointer(FriendlyByteBuf buffer, CurioSlotPointer slot) {
    buffer.writeUtf(slot.identifier);
    buffer.writeInt(slot.slot);
  }

  public static CurioSlotPointer readCurioSlotPointer(FriendlyByteBuf buffer) {
    return new CurioSlotPointer(buffer.readUtf(), buffer.readInt());
  }

  public static void writeCISlotPointer(FriendlyByteBuf buffer, CISlotPointer slot) {
    if (slot.isInventory()) {
      buffer.writeBoolean(true);
      buffer.writeInt(slot.getInventory());
    } else {
      buffer.writeBoolean(false);
      writeCurioSlotPointer(buffer, slot.getCurio());
    }
  }

  public static CISlotPointer readCISlotPointer(FriendlyByteBuf buffer) {
    if (buffer.readBoolean()) { // inventory
      return new CISlotPointer(buffer.readInt());
    } else { // curio
      return new CISlotPointer(readCurioSlotPointer(buffer));
    }
  }

  public static void writeBigStack(FriendlyByteBuf buffer, ItemStack stack, boolean limitedTag) {
    if (stack.isEmpty()) {
      buffer.writeBoolean(false);
    } else {
      buffer.writeBoolean(true);
      Item item = stack.getItem();
      buffer.writeVarInt(BuiltInRegistries.ITEM.getId(item));
      buffer.writeVarInt(stack.getCount());
      CompoundTag compoundNbt = limitedTag ? stack.getTag() : stack.getTag();
      buffer.writeNbt(compoundNbt);
    }
  }

  public static ItemStack readBigStack(FriendlyByteBuf buffer) {
    if (!buffer.readBoolean()) {
      return ItemStack.EMPTY;
    } else {
      int i = buffer.readVarInt();
      int j = buffer.readVarInt();
      ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.byId(i), j);
      itemstack.setTag(buffer.readNbt());
      return itemstack;
    }
  }

  public static void writeGlobalPos(FriendlyByteBuf buffer, GlobalPos pos) {
    CompoundTag nbt = (CompoundTag) NBTTagHelper.putGlobalPos(pos);
    buffer.writeNbt(nbt == null ? new CompoundTag() : nbt);
  }

  public static GlobalPos readGlobalPos(FriendlyByteBuf buffer) {
    CompoundTag nbt = buffer.readNbt();
    return NBTTagHelper.getGlobalPos(nbt);
  }

  public static void writeVector3f(FriendlyByteBuf buffer, Vector3f vec) {
    buffer.writeFloat(vec.getX());
    buffer.writeFloat(vec.getY());
    buffer.writeFloat(vec.getZ());
  }

  public static Vector3f readVector3f(FriendlyByteBuf buffer) {
    return new Vector3f(
        buffer.readFloat(),
        buffer.readFloat(),
        buffer.readFloat()
    );
  }

  public static void writeVector3d(FriendlyByteBuf buffer, Vector3d vec) {
    buffer.writeDouble(vec.getX());
    buffer.writeDouble(vec.getY());
    buffer.writeDouble(vec.getZ());
  }

  public static Vector3d readVector3d(FriendlyByteBuf buffer) {
    return new Vector3d(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    );
  }

  public static void writeVector3(FriendlyByteBuf buffer, Vector3 vec) {
    buffer.writeDouble(vec.x);
    buffer.writeDouble(vec.y);
    buffer.writeDouble(vec.z);
  }

  public static Vector3 readVector3(FriendlyByteBuf buffer) {
    return new Vector3(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    );
  }
}
