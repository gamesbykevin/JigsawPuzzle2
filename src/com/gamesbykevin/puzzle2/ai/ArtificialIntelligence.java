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
    
    public void solve(Puzzle pc)
    {
        if (pc.hasGameOver())
            return;
        
        if (!pc.hasSelectedPiece())
        {   //pick random selected piece with no children at least at first
            while(true)
            {
                int rand = (int)(Math.random() * pc.getPieces().size());
                Piece pp = pc.getPiece(rand);
                destination = pc.getDestination(pp);
                
                if (!pp.getPoint().equals(destination))
                {   //is this piece not already at the destination
                    pc.setSelectedPiece(rand);
                    pc.getTimerCollection().resetRemaining(Puzzle.TimerTrackers.CpuMoveTimer);
                    break;
                }
            }
        }
        
        Piece pp = pc.getCurrentPiece();
        
        movePiece(pp, pc.getTimerCollection().getTimer(Puzzle.TimerTrackers.CpuMoveTimer).getProgress());
        
        //after move now check if reached destination
        if (pp.getPoint().equals(destination))
        {   //destination reached check if pieces intersect to merge
            destination = null;
            pc.resetSelectedPiece();
            pc.mergePieces();
        }
        else
        {
            pc.setPiece(pc.getSelectedPiece(), pp);
        }
    }
    
    private void movePiece(Piece pp1, float progress)
    {
        Point start = pp1.getOriginalLocation();
        
        int xDiff = start.x - destination.x;
        int yDiff = start.y - destination.y;
        
        int x = destination.x;
        int y = destination.y;
        
        if (progress < 1)
        {
            x = start.x - (int)(xDiff * progress);
            y = start.y - (int)(yDiff * progress);
        }

        //dont forget to move any pieces connected to this one
        pp1.setNewPosition(x, y);
    }
}