package edgruberman.bukkit.livemarkers;

public enum KnownMarkers {

    ONLINE_PLAYER("4")
  , OFFLINE_PLAYER("5")
  , TAMED_WOLF("6")
  , TAMED_OCELOT("7")
  , BED_SPAWN("8")
  ;

  public final String id;

  KnownMarkers(final String id) {
      this.id = id;
  }

}
