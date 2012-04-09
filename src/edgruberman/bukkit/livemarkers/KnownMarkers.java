package edgruberman.bukkit.livemarkers;

/**
 * The first six (0-5) were defined in the original hMod MapMarkers plugin.
 */
public enum KnownMarkers {

    SPAWN("0")
  , HOME("1")
  , TOWN("2")
  , PLACE_OF_INTEREST("3")
  , ONLINE_PLAYER("4")
  , CAPITAL("5")
  , OFFLINE_PLAYER("6")
  , TAMED_WOLF("7")
  , TAMED_OCELOT("8")
  , BED_SPAWN("9")
  ;

  public final String id;

  KnownMarkers(final String id) {
      this.id = id;
  }

}
