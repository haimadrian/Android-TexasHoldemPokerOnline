package exc

/**
 *
 * @author Haim Adrian
 * @since 30-May-21
 */
class ColorPencil(length: Double, company: String, var color: String) : Pencil(length, company) {

    constructor(another: ColorPencil) : this(another.length, another.company, another.color)

    fun isColor(c: String): Boolean {
        return c === color
    }

    override fun toString(): String {
        return "${super.toString()}, color=$color"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorPencil) return false

        // Use the comparison from base class to cover length and company
        if (!super.equals(other)) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }
}
