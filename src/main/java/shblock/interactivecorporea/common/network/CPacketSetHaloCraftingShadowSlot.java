package shblock.interactivecorporea.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.function.Supplier;

public class CPacketSetHaloCraftingShadowSlot {
  private final CISlotPointer haloSlot;
  private final int slot;
  private final ItemStack shadowStack;

  public CPacketSetHaloCraftingShadowSlot(CISlotPointer haloSlot, int slot, ItemStack shadowStack) {
    this.haloSlot = haloSlot;
    this.slot = slot;
    this.shadowStack = shadowStack.copy();
  }

  public static CPacketSetHaloCraftingShadowSlot decode(FriendlyByteBuf buf) {
    return new CPacketSetHaloCraftingShadowSlot(
        NetworkHelper.readCISlotPointer(buf),
        buf.readInt(),
        NetworkHelper.readBigStack(buf)
    );
  }

  public void encode(FriendlyByteBuf buf) {
    NetworkHelper.writeCISlotPointer(buf, haloSlot);
    buf.writeInt(slot);
    NetworkHelper.writeBigStack(buf, shadowStack, false);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null || slot < 0 || slot > 8) {
        return;
      }

      ItemStack halo = haloSlot.getStack(player);
      if (!(halo.getItem() instanceof ItemRequestingHalo)) {
        return;
      }

      ItemRequestingHalo.setShadowStackInCraftingSlot(halo, slot, shadowStack);
      CPacketRequestItemListUpdate.broadcastRemoteState(player, halo);
    });
    ctx.get().setPacketHandled(true);
  }
}