package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.display.WindowHelper;
import com.gamesbykevin.framework.util.*;

import com.gamesbykevin.puzzle2.main.*;
import com.gamesbykevin.puzzle2.menu.GameMenu;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

import java.util.List;

public class Puzzles 
{
    //all the puzzles in the game
    private List<Puzzle> collection;
    
    //timer we will use to countdown next puzzle
    private Timer timer;
    
    public Puzzles()
    {
        
    }
    
    public boolean isEveryPuzzleFinished()
    {
        if (collection == null || collection.size() < 1)
            return false;
        
        //if game is finished and no place set
        for (Puzzle puzzle : collection)
        {
            if (puzzle == null)
                return false;
            
            if (!puzzle.hasGameOver() || !puzzle.hasPlace())
                return false;
        }
        
        return true;
    }
    
    public void reset(Engine engine)
    {
        this.timer = new Timer(TimerCollection.toNanoSeconds(6000L));
        
        int numPiecesIndex = engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Pieces);
        
        final int numPieces;
        
        switch (numPiecesIndex)
        {
            case 0:
                numPieces = 16;
                break;
            case 1:
                numPieces = 25;
                break;
            case 2:
                numPieces = 36;
                break;
            case 3:
                numPieces = 64;
                break;
            case 4:
                numPieces = 100;
                break;
            case 5:
                numPieces = 225;
                break;
            default:
                numPieces = 9;
                break;
        }
        
        //what are the dimensions of each puzzle
        int puzzleRows = (int)Math.sqrt(numPieces);
        int puzzleCols = (int)Math.sqrt(numPieces);

        boolean humanPlayer = (engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Human) == 0);
        
        final int difficultyIndex = engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Difficulty);
        final int gameTypeIndex   = engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Type);
        final int puzzleCutIndex  = engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.PuzzleCut);
        
        final int numPlayersIndex = engine.getGameMenu().getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.NumPlayers);
        
        final int numPlayers;
        
        switch (numPlayersIndex)
        {
            case 0:
                numPlayers = 1;
                break;
            case 1:
                numPlayers = 2;
                break;
            case 2:
                numPlayers = 4;
                break;
            case 3:
                numPlayers = 6;
                break;
            case 4:
                numPlayers = 9;
                break;
            case 5:
                numPlayers = 12;
                break;
            default:
                numPlayers = 1;
                break;
        }
        
        collection = new ArrayList<>();
        
        double squareRoot = Math.sqrt(numPlayers);
        
        Rectangle windows[][] = null;
        Rectangle leftSide = null;
        Rectangle rightSide = null;
        
        //if 1 player use entire window
        if (numPlayers == 1)
        {
            windows = WindowHelper.getWindows(engine.getMain().getScreen(), (int)squareRoot, (int)squareRoot);
        }
        else
        {
            if (humanPlayer)
            {
                //if 1st player is human they will get left half the window, the other cpu windows will all share the right half
                Rectangle r = engine.getMain().getScreen();
                leftSide  = new Rectangle(r.x,                        r.y, (int)(r.width * .75), r.height);
                rightSide = new Rectangle(r.x + (int)(r.width * .75), r.y, (int)(r.width * .25), r.height);
                
                if ((int)squareRoot < squareRoot)
                {
                    windows = WindowHelper.getWindows(rightSide, (int)squareRoot, (int)squareRoot + 1);
                }
                else
                {
                    windows = WindowHelper.getWindows(rightSide, (int)squareRoot, (int)squareRoot);
                }
            }
            else
            {
                //if 1st player isn't human everyone gets the same size screen
                if ((int)squareRoot < squareRoot)
                {
                    windows = WindowHelper.getWindows(engine.getMain().getScreen(), (int)squareRoot, (int)squareRoot + 1);
                }
                else
                {
                    windows = WindowHelper.getWindows(engine.getMain().getScreen(), (int)squareRoot, (int)squareRoot);
                }
            }
        }
        
        final Image image = engine.getResources().getGameImage();
        
        if (humanPlayer)
        {
            if (leftSide != null)
            {
                collection.add(new Puzzle(image, puzzleRows, puzzleCols, leftSide, engine.getMain().getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex));
            }
            else
            {
                collection.add(new Puzzle(image, puzzleRows, puzzleCols, windows[0][0], engine.getMain().getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex));
            }
        }
        
        for (int row=0; row < windows.length; row++)
        {
            for (int col=0; col < windows[0].length; col++)
            {
                if (collection.size() >= numPlayers)
                    continue;
                
                if (windows[row][col].width > windows[row][col].height)
                    windows[row][col].width = windows[row][col].height;
                else
                    windows[row][col].height = windows[row][col].width;
                
                Puzzle puzzle = new Puzzle(image, puzzleRows, puzzleCols, windows[row][col], engine.getMain().getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex);
                puzzle.setAutoSolve(true);
                collection.add(puzzle);
            }
        }
    }
    
    public void update(Engine engine) throws Exception
    {
        for (Puzzle puzzle : collection)
        {
            if (puzzle != null)
            {
                puzzle.update(engine.getMouse());
                
                if (puzzle.hasPlaySound())
                {
                    engine.getResources().playSound(ResourceManager.GameAudio.Connect, false);
                    puzzle.setPlaySound(false);
                }
            }
        }

        //need to check if game is finished and set place 1st, 2nd, 3rd etc..
        int nextPlace = 1;

        //calculate what the next rank will be
        for (Puzzle puzzle : collection)
        {
            if (puzzle != null && puzzle.hasGameOver() && puzzle.hasPlace())
                nextPlace++;
        }
        
        //if game is finished and no place set
        for (Puzzle puzzle : collection)
        {
            if (puzzle != null && puzzle.hasGameOver() && !puzzle.hasPlace())
            {
                puzzle.setPlace(nextPlace);
                nextPlace++;
            }
        }
        
        if (isEveryPuzzleFinished())
        {
            if (timer.hasTimePassed())
            {
                reset(engine);
                return;
            }
            
            timer.update(engine.getMain().getTimeDeductionPerFrame());
        }
    }
    
    public Graphics2D render(Graphics2D g2d, Engine engine)
    {
        if (isEveryPuzzleFinished())
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        
        if (collection != null)
        {
            for (Puzzle puzzle : collection)
            {
                if (puzzle != null)
                    puzzle.draw(g2d);
            }
        }
        
        if (isEveryPuzzleFinished())
        {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2d.setColor(Color.RED);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 24));
            final int middleX = engine.getMain().getScreen().x + (engine.getMain().getScreen().width  / 2);
            final int middleY = engine.getMain().getScreen().y + (engine.getMain().getScreen().height / 2);
            
            final String desc = "Next Puzzle in " + timer.getDescRemaining(TimerCollection.FORMAT_5);
            
            g2d.drawString(desc, middleX - (g2d.getFontMetrics().stringWidth(desc) / 2), middleY - (g2d.getFontMetrics().getHeight() / 2));
        }
        
        return g2d;
    }
}