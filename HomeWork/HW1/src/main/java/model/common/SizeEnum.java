package model.common;

/**
 * @author Haim Adrian
 * @since 04-Nov-20
 */
public enum SizeEnum {
    /** Use undefined as the first ordinal value for the enum (0) **/
    Undefined,
    Small,
    Medium,
    Large;

    /**
     * Tests whether this size enum fits in a given size enum.<br/>
     * <ul>
     *     <li>Small fits in all sizes</li>
     *     <li>Medium fits in Medium and Large only, but not in Small.</li>
     *     <li>Large fits in Large only, but not in Small or Medium</li>
     * </ul>
     * @param in The other size to test if this size enum fits in it
     * @return Whether this size enum fits in a given size enum or not
     */
    public boolean fitsIn(SizeEnum another) {
        return ordinal() <= another.ordinal();
    }
}

