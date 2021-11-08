package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.util.Comparator;
import java.util.Map.Entry;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

/**
 * Convenience interface for {@code Comparator<Entry<PlayerIDTag,
 * Pair<DiplomaticStatus, DiplomaticStatus>>>}
 * 
 * @author rbasamoyai
 */
public interface DiplomacyListComparator extends Comparator<Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>>> {
	
	public static DiplomacyListComparator noSort() {
		return new DiplomacyListComparator() {
			@Override
			public int compare(
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry1, 
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry2) {
				return 0;
			}
		};
	}
	
	public static DiplomacyListComparator sortByName(ClientPlayNetHandler netHandler) {
		return new DiplomacyListComparator() {	
			@Override
			public int compare(
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry1,
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry2) {
				PlayerIDTag tag1 = entry1.getKey();
				PlayerIDTag tag2 = entry2.getKey();
				
				// TODO: replace with actual NPC faction code
				String playerName1 = tag1.isPlayer()
						? netHandler.getPlayerInfo(tag1.getUUID()).getProfile().getName()
						: "NPC Faction";
				String playerName2 = tag2.isPlayer()
						? netHandler.getPlayerInfo(tag2.getUUID()).getProfile().getName()
						: "NPC Faction";
				
				return playerName1.compareTo(playerName2);
			}
		};
	}
	
	public static DiplomacyListComparator sortByIsPlayer() {
		return new DiplomacyListComparator() {
			@Override
			public int compare(
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry1,
					Entry<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> entry2) {
				boolean isPlayer1 = entry1.getKey().isPlayer();
				boolean isPlayer2 = entry2.getKey().isPlayer();
				
				if (isPlayer1 && isPlayer2) return 0;
				if (isPlayer1 && !isPlayer2) return -1;
				if (!isPlayer1 && isPlayer2) return 1;
				return 0;
			}
		};
	}
	
}
