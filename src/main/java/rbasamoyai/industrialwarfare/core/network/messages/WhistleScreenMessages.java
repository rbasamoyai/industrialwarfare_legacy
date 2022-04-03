package rbasamoyai.industrialwarfare.core.network.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import rbasamoyai.industrialwarfare.common.containers.WhistleContainer;
import rbasamoyai.industrialwarfare.common.entityai.CombatMode;
import rbasamoyai.industrialwarfare.common.entityai.formation.FormationAttackType;
import rbasamoyai.industrialwarfare.common.entityai.formation.UnitFormationType;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.FormationCategory;
import rbasamoyai.industrialwarfare.common.items.WhistleItem.Interval;
import rbasamoyai.industrialwarfare.core.IWModRegistries;

public class WhistleScreenMessages {
	
	public static class SWhistleScreenSync {
		public Interval interval;
		public CombatMode mode;
		public UnitFormationType<?> type;
		public Map<FormationCategory, UnitFormationType<?>> formationCategories;
		public Map<FormationCategory, FormationAttackType> attackTypes;
		
		public SWhistleScreenSync() {}
		
		public SWhistleScreenSync(Interval interval, CombatMode mode, UnitFormationType<?> type,
				Map<FormationCategory, UnitFormationType<?>> formationCategories,
				Map<FormationCategory, FormationAttackType> attackTypes) {
			this.interval = interval;
			this.mode = mode;
			this.type = type;
			this.formationCategories = formationCategories;
			this.attackTypes = attackTypes;
		}
		
		public static void encode(SWhistleScreenSync msg, PacketBuffer buf) {
			buf
			.writeVarInt(msg.interval.getId())
			.writeVarInt(msg.mode.getId())
			.writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, msg.type);
			
			buf.writeVarInt(msg.formationCategories.size());
			msg.formationCategories.forEach((k, v) -> buf.writeVarInt(k.getId()).writeRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES, v));
			
			buf.writeVarInt(msg.attackTypes.size());
			msg.attackTypes.forEach((k, v) -> buf.writeVarInt(k.getId()).writeRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES, v));
		}
		
		public static SWhistleScreenSync decode(PacketBuffer buf) {
			Interval interval = Interval.fromId(buf.readVarInt());
			CombatMode mode = CombatMode.fromId(buf.readVarInt());
			UnitFormationType<?> type = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES);
			
			Map<FormationCategory, UnitFormationType<?>> formationCategories = new HashMap<>();
			int sz = buf.readVarInt();
			for (int i = 0; i < sz; ++i) {
				FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
				UnitFormationType<?> catType = buf.readRegistryIdUnsafe(IWModRegistries.UNIT_FORMATION_TYPES);
				formationCategories.put(cat, catType);
			}
			
			Map<FormationCategory, FormationAttackType> attackTypes = new HashMap<>();
			int sz1 = buf.readVarInt();
			for (int i = 0; i < sz1; ++i) {
				FormationCategory cat = FormationCategory.fromId(buf.readVarInt());
				FormationAttackType attackType = buf.readRegistryIdUnsafe(IWModRegistries.FORMATION_ATTACK_TYPES);
				attackTypes.put(cat, attackType);
			}
			
			return new SWhistleScreenSync(interval, mode, type, formationCategories, attackTypes);
		}
		
		public static void handle(SWhistleScreenSync msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity player = ctx.getSender();
				Container ct = player.containerMenu;
				if (!(ct instanceof WhistleContainer)) return;
				WhistleContainer whistleCt = (WhistleContainer) ct;
				whistleCt.setInterval(msg.interval);
				whistleCt.setMode(msg.mode);
				whistleCt.setFormation(msg.type);
				msg.formationCategories.forEach(whistleCt::setCategoryType);
				msg.attackTypes.forEach(whistleCt::setCategoryAttackType);
				whistleCt.updateItem(player);
			});
			ctx.setPacketHandled(true);
		}
	}
	
	public static class SStopAction {
		public SStopAction() {}
		
		public static void encode(SStopAction msg, PacketBuffer buf) {}
		public static SStopAction decode(PacketBuffer buf) { return new SStopAction(); }
		
		public static void handle(SStopAction msg, Supplier<NetworkEvent.Context> sup) {
			NetworkEvent.Context ctx = sup.get();
			ctx.enqueueWork(() -> {
				ServerPlayerEntity player = ctx.getSender();
				Container ct = player.containerMenu;
				if (!(ct instanceof WhistleContainer)) return;
				WhistleContainer whistleCt = (WhistleContainer) ct;
				whistleCt.stopWhistle(player);
			});
			ctx.setPacketHandled(false);
		}
	}
	
}
