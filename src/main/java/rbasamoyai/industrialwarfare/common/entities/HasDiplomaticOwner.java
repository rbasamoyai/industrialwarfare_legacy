package rbasamoyai.industrialwarfare.common.entities;

import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public interface HasDiplomaticOwner {
	PlayerIDTag getDiplomaticOwner();
	default boolean hasOwner() { return !this.getDiplomaticOwner().equals(PlayerIDTag.NO_OWNER); }
}
