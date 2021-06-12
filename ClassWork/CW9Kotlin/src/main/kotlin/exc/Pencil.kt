package exc

/**
 *
 * @author Haim Adrian
 * @since 30-May-21
 */
open class Pencil(length: Double, val company: String) {
    var length: Double = length
        set(value) {
            field = when {
                (value > field) -> throw IllegalArgumentException("Increasing pencil length is prohibited. Was: $value, Current: $field")
                (value <= 0.0) -> throw IllegalArgumentException("Pencil length cannot be zero or negative. Was: $value")
                else -> value
            }
        }

    constructor(another: Pencil) : this(another.length, another.company)

    init {
        this.length = length
    }

    fun sharpen(x: Double) {
        length -= x
    }

    override fun toString(): String {
        return "company='$company', length=$length"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pencil) return false

        if (company != other.company) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = company.hashCode()
        result = 31 * result + length.hashCode()
        return result
    }
}
