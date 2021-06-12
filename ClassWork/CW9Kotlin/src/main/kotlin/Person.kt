/**
 *
 * @author Haim Adrian
 * @since 30-May-21
 */
open class Person(var name: String = "", age: Int = 20, var salary: Int = 5500) {
    var age: Int = 0
        set(value) {
            field = if (value >= 18)
                value
            else
                18
        }

    init {
        this.age = age
    }

    override fun toString(): String {
        return "name='$name', age=$age, salary=$salary"
    }
}
