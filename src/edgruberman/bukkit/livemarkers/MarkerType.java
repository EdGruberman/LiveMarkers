package edgruberman.bukkit.livemarkers;

public enum MarkerType {

    // first six were originally defined in the hMod MapMarkers plugin
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
    , SIGN_CHANGE("10")
    ;

    public final String id;

    private MarkerType(final String id) {
        this.id = id;
    }

}
