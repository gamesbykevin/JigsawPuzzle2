package com.gamesbykevin.puzzle2.ai;

import com.gamesbykevin.puzzle2.objects.Piece;
import com.gamesbykevin.puzzle2.objects.Puzzle;
import com.gamesbykevin.framework.util.*;

import java.awt.Point;

public class ArtificialIntelligence 
{
    //where is the correct place for the current piece
    private Point destination;
    
    public enum Difficulty
    {
        Easy, Medium, Hard
    }
    
    //how long will it take to place each puzzle piece in correct place
    public static final long TIME_EASY       = 9500;
    public static final long TIME_MEDIUM     = 7500;
    public static final long TIME_HARD       = 3250;
    public static final long TIME_DIFFERENCE = 350;
    
    public ArtificialIntelligence(Puzzle puzzle, Difficulty difficulty)
    {
        long milliSeconds = 0;
        long difference = (long)((Math.random() * (TIME_DIFFERENCE * 2)) - TIME_DIFFERENCE);
        
        switch(difficulty)
        {
            case Easy:
                milliSeconds = TIME_EASY;
                break;
                
            case Medium:
                milliSeconds = TIME_MEDIUM;
                break;
                
            case Hard:
                milliSeconds = TIME_HARD;
                break;
        }
        
        //add difference to cpu time so not all cpu will have same time
        milliSeconds += difference;
        
        puzzle.getTimerCollection().setReset(Puzzle.TimerKey.CpuMove, TimerCollection.toNanoSeconds(milliSeconds));
    }
    
    /**
     * Returns the milliseconds that the cpu
     * is going to delay for placing each 
     * puzzle piece. Easy, Medium, Hard
     * @param difficulty
     * @return long Millisecond delay
     */
    public static long getDifficultyDelay(Difficulty difficulty)
    {
        long milliSeconds = TIME_DIFFERENCE;
        
        if (difficulty == Difficulty.Easy)
            milliSeconds += TIME_EASY;
        if (difficulty == Difficulty.Medium)
            milliSeconds += TIME_MEDIUM;
        if (difficulty == Difficulty.Hard)
            milliSeconds += TIME_HARD;
        
        return TimerCollection.toNanoSeconds(milliSeconds);
    }
    
    public void solve(Puzzle puzzle)
    {
        if (puzzle.hasGameOver())
            return;
        
        //pick random selected piece with no children at least at first
        if (!puzzle.hasSelectedPiece())
        {
            while(true)
            {
                int rand = (int)(Math.random() * puzzle.getPieces().size());
                Piece piece = puzzle.getPieces().get(rand);
                destination = puzzle.getDestination(piece);
                
                //is this piece not already at the destination
                if (!piece.getPoint().equals(destination))
                {
                    puzzle.setSelectedPieceIndex(rand);
                    puzzle.getTimerCollection().resetRemaining(Puzzle.TimerKey.CpuMove);
                    break;
                }
            }
        }
        
        Piece piece = puzzle.getPiece();
        
        movePiece(piece, puzzle);
        
        //after move now check if reached destination
        if (piece.getPoint().equals(destination))
        {   //destination reached check if pieces intersect to merge
            destination = null;
            puzzle.resetSelectedPieceIndex();
            puzzle.mergePieces();
        }
        else
        {
            puzzle.setPiece(puzzle.getSelectedPieceIndex(), piece);
        }
    }
    
    private void movePiece(final Piece piece, final Puzzle puzzle)
    {
        Point start = piece.getOriginalLocation();
        
        int xDiff = start.x - destination.x;
        int yDiff = start.y - destination.y;
        
        int x = destination.x;
        int y = destination.y;
        
        if (puzzle.getTimerCollection().getTimer(Puzzle.TimerKey.CpuMove).getProgress() < 1)
        {
            x = start.x - (int)(xDiff * puzzle.getTimerCollection().getTimer(Puzzle.TimerKey.CpuMove).getProgress());
            y = start.y - (int)(yDiff * puzzle.getTimerCollection().getTimer(Puzzle.TimerKey.CpuMove).getProgress());
        }
        else
        {
            //piece is set so play connect sound effect
            puzzle.setPlaySound(true);
        }

        //dont forget to move any child pieces connected to this one
        piece.setNewPosition(new Point(x, y));
    }
}