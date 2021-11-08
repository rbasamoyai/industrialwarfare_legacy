package rbasamoyai.industrialwarfare.client.screen.diplomacy;

import java.util.HashMap;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.gui.screen.Screen;
import rbasamoyai.industrialwarfare.common.diplomacy.DiplomaticStatus;
import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public interface IFetchDiplomacyData {

	default Map<PlayerIDTag, Pair<DiplomaticStatus, DiplomaticStatus>> getStatuses(Screen screen) {
		return screen instanceof DiplomacyScreen
				? ((DiplomacyScreen) screen).getMenu().getDiplomaticStatuses()
				: new HashMap<>();
	}

	
}
