/**
 *
 * @author Haim Adrian
 * @since 30-May-21
 */
class Enemy(name: String, age: Int = 20, salary: Int = 5500, var hp: Int, val hitPoints: Int) : Person(name, age, salary) {
    fun hit(hitPoints: Int) {
        this.hp = this.hp - hitPoints
    }

    override fun toString(): String {
        return "Enemy(${super.toString()}, hp=$hp, hitPoints=$hitPoints)"
    }
}
