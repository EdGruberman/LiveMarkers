package edgruberman.bukkit.livemarkers;

/**
 * Ensures MarkerCache subclasses provide the marker ID.
 */
public interface MarkerIdentifier {

    /**
     * ID used to distinguish the type of marker in the JSON output.
     */
    public abstract int getId();

}
