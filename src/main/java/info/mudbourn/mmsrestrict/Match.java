package info.mudbourn.mmsrestrict;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

// One entry of a rule's match list: an id, an item tag, or a whole namespace.
public record Match(Kind kind, String raw, String namespace, Identifier id, TagKey<Item> tag) {

    public enum Kind {
        ID,
        TAG,
        NAMESPACE
    }

    public static Match parse(String raw) {
        if (raw.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(raw.substring(1));
            return tagId == null ? null : new Match(Kind.TAG, raw, null, null, TagKey.create(Registries.ITEM, tagId));
        }
        if (raw.endsWith(":*")) {
            String namespace = raw.substring(0, raw.length() - 2);
            return Identifier.isValidNamespace(namespace) ? new Match(Kind.NAMESPACE, raw, namespace, null, null) : null;
        }
        Identifier id = Identifier.tryParse(raw);
        return id == null ? null : new Match(Kind.ID, raw, null, id, null);
    }

    public boolean matchesId(Identifier candidate) {
        return switch (kind) {
            case ID -> id.equals(candidate);
            case NAMESPACE -> namespace.equals(candidate.getNamespace());
            case TAG -> false;
        };
    }
}
