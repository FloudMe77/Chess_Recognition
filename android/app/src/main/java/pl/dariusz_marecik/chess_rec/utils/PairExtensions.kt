package pl.dariusz_marecik.chess_rec.utils

// Extension operators for board coordinates represented as Pair<Int, Int>

// Subtracts two coordinate pairs
operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) =
    Pair(this.first - other.first, this.second - other.second)

// Adds two coordinate pairs
operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) =
    Pair(this.first + other.first, this.second + other.second)

// Divides both coordinates by an integer
operator fun Pair<Int, Int>.div(other: Int) =
    Pair(this.first / other, this.second / other)

// Multiplies both coordinates by an integer
operator fun Pair<Int, Int>.times(other: Int) =
    Pair(this.first * other, this.second * other)

// Multiplies two coordinate pairs element-wise
operator fun Pair<Int, Int>.times(other: Pair<Int, Int>) =
    Pair(this.first * other.first, this.second * other.second)
