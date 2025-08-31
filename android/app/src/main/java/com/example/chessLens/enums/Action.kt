package com.example.chessLens.enums

// Represents the type of action a chess move performs
enum class Action {
    TAKE,          // Capturing an opponent's piece
    MOVE,          // Regular move to an empty square
    CASTLE_LEFT,   // Queenside castling
    CASTLE_RIGHT,  // Kingside castling
    EN_PASSANT,    // Special pawn capture
    PROMOTION      // Pawn promotion
}
