package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.item.HaloInterfaceStyle;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SPacketRemoteRequestingHaloState {
  private static final int CRAFTING_SLOT_COUNT = 9;

  private final int playerId;
  private final boolean open;
  private final float rotationOffset;
  private final int listHeight;
  private final boolean sortByAmount;
  private final HaloInterfaceStyle interfaceStyle;
  private final int haloTint;
  private final List<ItemStack> itemList;
  private final boolean hasCraftingModule;
  private final List<ItemStack> craftingSlots;
  private final List<ItemStack> craftingShadowSlots;

  public SPacketRemoteRequestingHaloState(int playerId, boolean open, float rotationOffset, int listHeight, boolean sortByAmount, HaloInterfaceStyle interfaceStyle, int haloTint, List<ItemStack> itemList, boolean hasCraftingModule, List<ItemStack> craftingSlots, List<ItemStack> craftingShadowSlots) {
    this.playerId = playerId;
    this.open = open;
    this.rotationOffset = rotationOffset;
    this.listHeight = listHeight;
    this.sortByAmount = sortByAmount;
    this.interfaceStyle = interfaceStyle;
    this.haloTint = haloTint;
    this.itemList = itemList;
    this.hasCraftingModule = hasCraftingModule;
    this.craftingSlots = craftingSlots;
    this.craftingShadowSlots = craftingShadowSlots;
  }

  public static SPacketRemoteRequestingHaloState open(int playerId, float rotationOffset, int listHeight, boolean sortByAmount, HaloInterfaceStyle interfaceStyle, int haloTint, List<ItemStack> itemList, boolean hasCraftingModule, List<ItemStack> craftingSlots, List<ItemStack> craftingShadowSlots) {
    return new SPacketRemoteRequestingHaloState(playerId, true, rotationOffset, listHeight, sortByAmount, interfaceStyle, haloTint, itemList, hasCraftingModule, craftingSlots, craftingShadowSlots);
  }

  public static SPacketRemoteRequestingHaloState close(int playerId) {
    return new SPacketRemoteRequestingHaloState(playerId, false, 0F, 5, false, HaloInterfaceStyle.CLASSIC, 0xFFFFFF, Collections.emptyList(), false, Collections.emptyList(), Collections.emptyList());
  }

  public static SPacketRemoteRequestingHaloState decode(FriendlyByteBuf buf) {
    int playerId = buf.readVarInt();
    boolean open = buf.readBoolean();
    if (!open) {
      return close(playerId);
    }

    float rotationOffset = buf.readFloat();
    int listHeight = buf.readVarInt();
    boolean sortByAmount = buf.readBoolean();
    HaloInterfaceStyle interfaceStyle = buf.readEnum(HaloInterfaceStyle.class);
    int haloTint = buf.readInt();
    int len = buf.readVarInt();
    List<ItemStack> itemList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      itemList.add(NetworkHelper.readBigStack(buf));
    }
    boolean hasCraftingModule = buf.readBoolean();
    List<ItemStack> craftingSlots = readStacks(buf);
    List<ItemStack> craftingShadowSlots = readStacks(buf);
    return open(playerId, rotationOffset, listHeight, sortByAmount, interfaceStyle, haloTint, itemList, hasCraftingModule, craftingSlots, craftingShadowSlots);
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeVarInt(playerId);
    buf.writeBoolean(open);
    if (!open) {
      return;
    }

    buf.writeFloat(rotationOffset);
    buf.writeVarInt(listHeight);
    buf.writeBoolean(sortByAmount);
    buf.writeEnum(interfaceStyle);
    buf.writeInt(haloTint);
    buf.writeVarInt(itemList.size());
    for (ItemStack stack : itemList) {
      NetworkHelper.writeBigStack(buf, stack, false);
    }
    buf.writeBoolean(hasCraftingModule);
    writeStacks(buf, craftingSlots);
    writeStacks(buf, craftingShadowSlots);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> RequestingHaloInterfaceHandler.handleRemoteState(playerId, open, rotationOffset, listHeight, sortByAmount, itemList, interfaceStyle, haloTint, hasCraftingModule, craftingSlots, craftingShadowSlots));
    ctx.get().setPacketHandled(true);
  }

  private static List<ItemStack> readStacks(FriendlyByteBuf buf) {
    List<ItemStack> stacks = new ArrayList<>(CRAFTING_SLOT_COUNT);
    for (int i = 0; i < CRAFTING_SLOT_COUNT; i++) {
      stacks.add(NetworkHelper.readBigStack(buf));
    }
    return stacks;
  }

  private static void writeStacks(FriendlyByteBuf buf, List<ItemStack> stacks) {
    for (int i = 0; i < CRAFTING_SLOT_COUNT; i++) {
      NetworkHelper.writeBigStack(buf, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY, false);
    }
  }
}