package exc

import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val pencil1 = Pencil(15.0, "Kotlin")
    val pencil2 = Pencil(pencil1)
    val colorPencil1 = ColorPencil(15.0, "Kotlin", "Blue")
    val colorPencil2 = ColorPencil(15.0, "Kotlin", "Green")
    val colorPencil3 = ColorPencil(colorPencil2)

    println("Pencil1($pencil1)")
    println("Pencil2($pencil2)")
    println("colorPencil1($colorPencil1)")
    println("colorPencil2($colorPencil2)")
    println("colorPencil3($colorPencil3)")

    try {
        val pencil3 = Pencil(-15.0, "Kotlin")
    } catch (e: IllegalArgumentException) {
        println("Caught exception when trying to create a pencil with negative length: $e")
    }

    println("${System.lineSeparator()}Pencil1 == Pencil2 ? ${pencil1 == pencil2}")

    pencil2.sharpen(2.5)
    println("Pencil2 after sharpening it by 2.5: $pencil2")
    println("Pencil1 == Pencil2 ? ${pencil1 == pencil2}")

    println("colorPencil1.isColor(\"Blue\"): ${colorPencil1.isColor("Blue")}")
    println("colorPencil1.isColor(\"blue\"): ${colorPencil1.isColor("blue")}")
    println("colorPencil1.isColor(\"Green\"): ${colorPencil1.isColor("Green")}")

    println("colorPencil1 == colorPencil2 ? ${colorPencil1 == colorPencil2}")
    println("colorPencil2 == colorPencil3 ? ${colorPencil2 == colorPencil3}")

    try {
        colorPencil1.sharpen(20.0)
    } catch (e: IllegalArgumentException) {
        println("Caught exception after invoking (colorPencil1.sharpen(20.0)): $e")
    }

    try {
        colorPencil1.length = 0.0
    } catch (e: IllegalArgumentException) {
        println("Caught exception after invoking (colorPencil1.length = 0.0): $e")
    }

    try {
        colorPencil1.length = 20.0
    } catch (e: IllegalArgumentException) {
        println("Caught exception after invoking (colorPencil1.length = 20.0): $e")
    }

}
