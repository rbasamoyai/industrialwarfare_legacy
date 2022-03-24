package rbasamoyai.industrialwarfare.common.entities;

import rbasamoyai.industrialwarfare.common.diplomacy.PlayerIDTag;

public interface IHasDiplomaticOwner {
	PlayerIDTag getDiplomaticOwner();
	default boolean hasOwner() { return !this.getDiplomaticOwner().equals(PlayerIDTag.NO_OWNER); }
}
