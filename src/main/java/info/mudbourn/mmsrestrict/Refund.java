package info.mudbourn.mmsrestrict;

import net.minecraft.resources.Identifier;

// The item a swept item is replaced with, and how many per swept item.
public record Refund(Identifier item, int count) {
}
