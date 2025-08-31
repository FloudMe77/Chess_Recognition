package com.example.chessLens

import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import com.example.chessLens.enums.Action
import com.example.chessLens.enums.ColorTeam
import com.example.chessLens.enums.PieceKind
import com.example.chessLens.game.PositionManager
import com.example.chessLens.utils.Move
import com.example.chessLens.utils.PieceInfo
import com.example.chessLens.utils.plus

class PositionManagerTest{
  val normalPositionMap: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (0 to 6) to PieceInfo(6, "a7", emptyList()),
   (1 to 4) to PieceInfo(0, "b5", emptyList()),
   (1 to 7) to PieceInfo(7, "b8", emptyList()),
   (2 to 2) to PieceInfo(5, "c3", emptyList()),
   (2 to 5) to PieceInfo(6, "c6", emptyList()),
   (2 to 6) to PieceInfo(2, "c7", emptyList()),
   (2 to 7) to PieceInfo(9, "c8", emptyList()),
   (4 to 3) to PieceInfo(8, "e4", emptyList()),
   (4 to 6) to PieceInfo(7, "e7", emptyList()),
   (5 to 1) to PieceInfo(0, "f2", emptyList()),
   (5 to 6) to PieceInfo(0, "f7", emptyList()),
   (5 to 7) to PieceInfo(11, "f8", emptyList()),
   (6 to 6) to PieceInfo(6, "g7", emptyList()),
   (7 to 3) to PieceInfo(3, "h4", emptyList()),
  )
  val matePositionMap: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (1 to 4) to PieceInfo(6, "b5", emptyList()),
   (2 to 1) to PieceInfo(4, "c2", emptyList()),
   (2 to 2) to PieceInfo(2, "c3", emptyList()),
   (3 to 4) to PieceInfo(6, "d5", emptyList()),
   (4 to 5) to PieceInfo(1, "e6", emptyList()),
   (5 to 3) to PieceInfo(11, "f4", emptyList()),
   (6 to 3) to PieceInfo(10, "g4", emptyList()),
   (6 to 6) to PieceInfo(3, "g7", emptyList()),
   (7 to 3) to PieceInfo(5, "h4", emptyList())
  )
  val notMatePositionMap: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (1 to 4) to PieceInfo(6, "b5", emptyList()),
   (2 to 1) to PieceInfo(4, "c2", emptyList()),
   (2 to 2) to PieceInfo(2, "c3", emptyList()),
   (3 to 4) to PieceInfo(6, "d5", emptyList()),
   (4 to 5) to PieceInfo(1, "e6", emptyList()),
   (5 to 3) to PieceInfo(11, "f4", emptyList()),
   (6 to 3) to PieceInfo(10, "g4", emptyList()),
   (6 to 6) to PieceInfo(3, "g7", emptyList()),
   (7 to 3) to PieceInfo(5, "h4", emptyList()),
   (5 to 1) to PieceInfo(2, "f2", emptyList()),
  )
  val staleMatePositionMap: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (1 to 4) to PieceInfo(6, "b5", emptyList()),
   (3 to 4) to PieceInfo(6, "d5", emptyList()),
   (5 to 3) to PieceInfo(11, "f4", emptyList()),
   (5 to 4) to PieceInfo(10, "f5", emptyList()),
   (7 to 3) to PieceInfo(5, "h4", emptyList()),
   (7 to 2) to PieceInfo(0, "h3", emptyList()),
  )
  val elPassantOnlyValidMove: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (0 to 0) to PieceInfo(11, "a1", emptyList()),
   (3 to 6) to PieceInfo(6, "d7", emptyList()),
   (4 to 4) to PieceInfo(0, "e5", emptyList()),
   (5 to 2) to PieceInfo(10, "f3", emptyList()),
   (7 to 3) to PieceInfo(5, "h4", emptyList()),
   (4 to 5) to PieceInfo(8, "e6", emptyList())
  )
  val elPassantOnlyValidMove2: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
   (0 to 0) to PieceInfo(11, "a1", emptyList()),
   (3 to 4) to PieceInfo(6, "d5", emptyList()),
   (4 to 4) to PieceInfo(0, "e5", emptyList()),
   (5 to 2) to PieceInfo(10, "f3", emptyList()),
   (7 to 3) to PieceInfo(5, "h4", emptyList()),
   (4 to 5) to PieceInfo(8, "e6", emptyList())
  )

  fun takeTest(from: Pair<Int,Int>, to: Pair<Int,Int>){
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = normalPositionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)
   normalPositionMap[from]?.let { piece ->
    val positionManager = PositionManager(normalPositionMap, piece.color)

    assertFalse(positionManager.isKingAttacked(normalPositionMap, ColorTeam.BLACK))
    assertTrue(positionManager.isKingAttacked(normalPositionMap, ColorTeam.WHITE))
    assertEquals(
     Move(from, to, piece.name, Action.TAKE),
     positionManager.findLegalMove(piecesMapAfter, setOf(from), setOf())
    )

    for (coords in normalPositionMap.keys) {
     if (coords == from) continue
     assertNull(positionManager.findLegalMove(piecesMapAfter, setOf(coords), setOf()))
    }
   } ?: run {
    fail("Brak figury na polu $from") // np. test ma się nie udać, jeśli brak
   }

  }

  fun moveTest(from: Pair<Int, Int>, ablePairs:List<Pair<Int,Int>>){

   normalPositionMap[from]?.let { piece ->
    val positionManager = PositionManager(normalPositionMap, piece.color)
    assertEquals(ablePairs.toSet(), piece.movement.possibleMove(from, normalPositionMap).toSet())

    val allPairs = (0..7).flatMap { x -> (0..7).map { y -> x to y } }
    val unablePairs = allPairs - ablePairs
    for(to in ablePairs){
     assertEquals(Move(from, to, piece.name, Action.MOVE), positionManager.findLegalMove(normalPositionMap, setOf(from), setOf(to)))
    }
    for(to in unablePairs){
     assertNull(positionManager.findLegalMove(normalPositionMap, setOf(from), setOf(to)))
    }
  } ?: run {
   fail("Brak figury na polu $from") // np. test ma się nie udać, jeśli brak
  }
  }



 @Test
 fun knightTakeTest(){
  takeTest(Pair(4,3), Pair(5, 1))
 }
  @Test
  fun rookTakeTest(){
   val from = Pair(1,7)
   val to = Pair(1,4)
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = normalPositionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)


   normalPositionMap[from]?.let {
    val positionManager = PositionManager(normalPositionMap, it.color)
    assertFalse(positionManager.isKingAttacked(normalPositionMap, ColorTeam.BLACK))
    assertTrue(positionManager.isKingAttacked(normalPositionMap, ColorTeam.WHITE))
    assertEquals(Move(from, to, it.name, Action.TAKE), positionManager.findLegalMove(piecesMapAfter, setOf(from), setOf()))
   }?: run {
    fail("Brak figury na polu $from") // np. test ma się nie udać, jeśli brak
   }
  }
  @Test
  fun pawnTakeTest(){
   val from = Pair(1,4)
   val to = Pair(2,5)
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = normalPositionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)

   val positionManager = PositionManager(normalPositionMap)
   assertFalse(positionManager.isKingAttacked(normalPositionMap, ColorTeam.BLACK))
   assertTrue(positionManager.isKingAttacked(normalPositionMap, ColorTeam.WHITE))
   assertEquals(Move(from, to, PieceKind.WHITE_PAWN, Action.TAKE), positionManager.findLegalMove(piecesMapAfter, setOf(from), setOf()))
  }
  @Test
  fun bishopTakeTest() {
   takeTest(Pair(7,3), Pair(4,6))
  }
  @Test
  fun kingTakeTest() {
   val from = Pair(5,7)
   val to = Pair(5,6)
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = normalPositionMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)


   normalPositionMap[from]?.let {
    val positionManager = PositionManager(normalPositionMap, it.color)
    assertFalse(positionManager.isKingAttacked(normalPositionMap, ColorTeam.BLACK))
    assertTrue(positionManager.isKingAttacked(normalPositionMap, ColorTeam.WHITE))
    assertEquals(Move(from, to, it.name, Action.TAKE), positionManager.findLegalMove(piecesMapAfter, setOf(from), setOf()))
   }?: run {
    fail("Brak figury na polu $from") // np. test ma się nie udać, jeśli brak
   }

  }

  // zwykłe przesunięcie
  @Test
  fun kingMoveTest(){
   val from = Pair(2,2)
   val tos = (-1..1).flatMap { x -> (-1..1).map { y -> from + (x to y) } } - from
   moveTest(from, tos)
  }
  @Test
  fun pawnMoveTest(){
   var from = Pair(1,4)
   var tos = listOf(Pair(1,5))
   moveTest(from, tos)
   from = Pair(2,5)
   tos = listOf(Pair(2,4))
   moveTest(from, tos)
  }
  @Test
  fun rookMoveTest(){
   val from = Pair(4,6)
   val tos = listOf(Pair(4,4), Pair(4,5), Pair(3,6), Pair(4,7))
   moveTest(from, tos)
  }
  @Test
  fun bishopMoveTest(){
   val from = Pair(7,3)
   val tos = listOf(Pair(6,2), Pair(5,5), Pair(6,4))
   moveTest(from, tos)
  }
  @Test
  fun knightMoveTest(){
   val from = Pair(2,6)
   val tos = listOf(Pair(0,7), Pair(0,5), Pair(3,4), Pair(4,5), Pair(4,7))
   moveTest(from, tos)
  }
  @Test
  fun mateTest(){
   val positionManager = PositionManager(matePositionMap)
   assertFalse(positionManager.haveAnyLegalMove(matePositionMap))
   assertFalse(positionManager.haveAnyLegalMove(staleMatePositionMap))
   assertTrue(positionManager.haveAnyLegalMove(notMatePositionMap))
  }

  @Test
  fun onlyElPassantLegal(){
   val positionManager = PositionManager(elPassantOnlyValidMove, ColorTeam.BLACK)
   positionManager.considerNewPosition(elPassantOnlyValidMove2)
   println(positionManager.acceptNewState())
   assertTrue(positionManager.haveAnyLegalMove(elPassantOnlyValidMove2))
  }

 }