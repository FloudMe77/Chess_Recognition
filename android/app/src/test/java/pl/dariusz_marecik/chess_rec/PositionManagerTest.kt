package pl.dariusz_marecik.chess_rec

import org.junit.Test
import org.junit.jupiter.api.Assertions.*
 class PositionManagerTest{
  val piecesMap: MutableMap<Pair<Int,Int>, PieceInfo> = mutableMapOf(
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

  fun takeTest(from: Pair<Int,Int>, to: Pair<Int,Int>){
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = piecesMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)

   val positionManager = PositionManager(piecesMap)
   assertFalse(positionManager.isKingAttacked(piecesMap, ColorTeam.BLACK))
   assertTrue(positionManager.isKingAttacked(piecesMap, ColorTeam.WHITE))
   assertEquals(Triple(from, to, Action.TAKE), positionManager.findLegalMove(setOf(from), setOf(),piecesMapAfter))

   //reszta błędna
   for(cords in piecesMap.keys){
    if(cords.equals(from)) continue
    assertNull(positionManager.findLegalMove(setOf(cords), setOf(), piecesMapAfter))
   }
  }

  fun moveTest(from: Pair<Int, Int>, ablePairs:List<Pair<Int,Int>>){
   val positionManager = PositionManager(piecesMap)
   val allPairs = (0..7).flatMap { x -> (0..7).map { y -> x to y } }
   val unablePairs = allPairs - ablePairs
   for(to in ablePairs){
    assertEquals(Triple(from, to, Action.MOVE), positionManager.findLegalMove(setOf(from), setOf(to), piecesMap))
   }
   for(to in unablePairs){
    assertNull(positionManager.findLegalMove(setOf(from), setOf(to), piecesMap))
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
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = piecesMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)

   val positionManager = PositionManager(piecesMap)
   assertFalse(positionManager.isKingAttacked(piecesMap, ColorTeam.BLACK))
   assertTrue(positionManager.isKingAttacked(piecesMap, ColorTeam.WHITE))
   assertEquals(Triple(from, to, Action.TAKE), positionManager.findLegalMove(setOf(from), setOf(),piecesMapAfter))
  }
  @Test
  fun pawnTakeTest(){
   val from = Pair(1,4)
   val to = Pair(2,5)
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = piecesMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)

   val positionManager = PositionManager(piecesMap)
   assertFalse(positionManager.isKingAttacked(piecesMap, ColorTeam.BLACK))
   assertTrue(positionManager.isKingAttacked(piecesMap, ColorTeam.WHITE))
   assertEquals(Triple(from, to, Action.TAKE), positionManager.findLegalMove(setOf(from), setOf(),piecesMapAfter))
  }
  @Test
  fun bishopTakeTest() {
   takeTest(Pair(7,3), Pair(4,6))
  }
  @Test
  fun kingTakeTest() {
   val from = Pair(5,7)
   val to = Pair(5,6)
   val piecesMapAfter: MutableMap<Pair<Int, Int>, PieceInfo> = piecesMap.mapValues { (_, v) -> v.deepCopy() }.toMutableMap()
   piecesMapAfter[to] = piecesMapAfter[from]!!
   piecesMapAfter.remove(from)

   val positionManager = PositionManager(piecesMap)
   assertFalse(positionManager.isKingAttacked(piecesMap, ColorTeam.BLACK))
   assertTrue(positionManager.isKingAttacked(piecesMap, ColorTeam.WHITE))
   assertEquals(Triple(from, to, Action.TAKE), positionManager.findLegalMove(setOf(from), setOf(),piecesMapAfter))
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

 }