public enum Move {
    WEST (0, -1, "left"),
    EAST (0, 1, "right"),
    NORTHWEST (-1, -1, "up-left"),
    NORTHEAST (-1, 0, "up-right"),
    SOUTHWEST (1, 1, "down-right"),
    SOUTHEAST (1, 0, "down-left");

    public final int r;
    public final int c;
    public final String s;
    Move(int r, int c, String s) {
        this.r = r;
        this.c = c;
        this.s = s;
    }
}