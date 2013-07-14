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
    public static final long TIME_EASY       = 5500;
    public static final long TIME_MEDIUM     = 3500;
    public static final long TIME_HARD       = 2750;
    public static final long TIME_DIFFERENCE = 250;
    
    public ArtificialIntelligence(Puzzle pc, Difficulty difficulty)
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
        
        pc.getTimerCollection().setReset(Puzzle.TimerTrackers.CpuMoveTimer, TimerCollection.toNanoSeconds(milliSeconds));
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
        
        if (!puzzle.hasSelectedPiece())
        {   //pick random selected piece with no children at least at first
            while(true)
            {
                int rand = (int)(Math.random() * puzzle.getPieces().size());
                Piece piece = puzzle.getPieces().get(rand);
                destination = puzzle.getDestination(piece);
                
                if (!piece.getPoint().equals(destination))
                {   //is this piece not already at the destination
                    puzzle.setSelectedPieceIndex(rand);
                    puzzle.getTimerCollection().resetRemaining(Puzzle.TimerTrackers.CpuMoveTimer);
                    break;
                }
            }
        }
        
        Piece piece = puzzle.getPiece();
        
        movePiece(piece, puzzle.getTimerCollection().getTimer(Puzzle.TimerTrackers.CpuMoveTimer).getProgress());
        
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
    
    private void movePiece(Piece piece, final float progress)
    {
        Point start = piece.getOriginalLocation();
        
        int xDiff = start.x - destination.x;
        int yDiff = start.y - destination.y;
        
        int x = destination.x;
        int y = destination.y;
        
        if (progress < 1)
        {
            x = start.x - (int)(xDiff * progress);
            y = start.y - (int)(yDiff * progress);
        }

        //dont forget to move any child pieces connected to this one
        piece.setNewPosition(new Point(x, y));
    }
}