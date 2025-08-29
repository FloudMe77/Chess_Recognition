package pl.dariusz_marecik.chess_rec.utils

operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = Pair(this.first - other.first, this.second - other.second)
operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = Pair(this.first + other.first, this.second + other.second)
operator fun Pair<Int, Int>.div(other: Int) = Pair(this.first / other, this.second / other)
operator fun Pair<Int, Int>.times(other: Int) = Pair(this.first * other, this.second * other)
operator fun Pair<Int, Int>.times(other: Pair<Int, Int>) = Pair(this.first * other.first, this.second * other.second)