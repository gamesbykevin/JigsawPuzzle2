package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.display.WindowHelper;
import com.gamesbykevin.framework.input.Mouse;

import com.gamesbykevin.puzzle2.main.Main;
import com.gamesbykevin.puzzle2.menu.GameMenu;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

import java.util.List;

public class Puzzles 
{
    private List<Puzzle> collection;
    
    public Puzzles(Main main, GameMenu menu, final Image image)
    {
        int numPiecesIndex = menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Pieces);
        
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

        boolean humanPlayer = (menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Human) == 0);
        
        final int difficultyIndex = menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Difficulty);
        final int gameTypeIndex   = menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.Type);
        final int puzzleCutIndex  = menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.PuzzleCut);
        
        final int numPlayersIndex = menu.getOptionSelectionIndex(GameMenu.LayerKey.Options, GameMenu.OptionKey.NumPlayers);
        
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
        Rectangle leftHalf = null;
        Rectangle rightHalf = null;
        
        //if 1 player use entire window
        if (numPlayers == 1)
        {
            windows = WindowHelper.getWindows(main.getScreen(), (int)squareRoot, (int)squareRoot);
        }
        else
        {
            if (humanPlayer)
            {
                //if 1st player is human they will get left half the window, the other cpu windows will all share the right half
                Rectangle r = main.getScreen();
                leftHalf = new Rectangle(r.x, r.y, r.width / 2, r.height);
                rightHalf = new Rectangle(r.x + (r.width / 2), r.y, r.width / 2, r.height);
                
                if ((int)squareRoot < squareRoot)
                {
                    windows = WindowHelper.getWindows(rightHalf, (int)squareRoot, (int)squareRoot + 1);
                }
                else
                {
                    windows = WindowHelper.getWindows(rightHalf, (int)squareRoot, (int)squareRoot);
                }
            }
            else
            {
                //if 1st player isn't human everyone gets the same size screen
                if ((int)squareRoot < squareRoot)
                {
                    windows = WindowHelper.getWindows(main.getScreen(), (int)squareRoot, (int)squareRoot + 1);
                }
                else
                {
                    windows = WindowHelper.getWindows(main.getScreen(), (int)squareRoot, (int)squareRoot);
                }
            }
        }
        
        if (humanPlayer)
        {
            if (leftHalf != null)
            {
                collection.add(new Puzzle(image, puzzleRows, puzzleCols, leftHalf, main.getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex));
            }
            else
            {
                collection.add(new Puzzle(image, puzzleRows, puzzleCols, windows[0][0], main.getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex));
            }
        }
        
        for (int row=0; row < windows.length; row++)
        {
            for (int col=0; col < windows[0].length; col++)
            {
                if (collection.size() >= numPlayers)
                    continue;
                
                Puzzle pc = new Puzzle(image, puzzleRows, puzzleCols, windows[row][col], main.getTimeDeductionPerFrame(), gameTypeIndex, difficultyIndex, puzzleCutIndex);
                pc.setAutoSolve(true);
                collection.add(pc);
            }
        }
    }
    
    public void update(Mouse mouse) throws Exception
    {
        for (Puzzle pc : collection)
        {
            if (pc != null)
                pc.update(mouse);
        }

        //need to check if game is finished and set place 1st, 2nd, 3rd etc..
        int nextPlace = 1;

        //calculate what the next rank will be
        for (Puzzle pc : collection)
        {
            if (pc != null && pc.hasGameOver() && pc.hasPlace())
                nextPlace++;
        }

        //if game is finished and no place set
        for (Puzzle pc : collection)
        {
            if (pc != null && pc.hasGameOver() && !pc.hasPlace())
            {
                pc.setPlace(nextPlace);
                nextPlace++;
            }
        }
    }
    
    public Graphics2D render(Graphics2D g2d)
    {
        if (collection != null)
        {
            for (Puzzle pc : collection)
            {
                if (pc != null)
                    pc.draw(g2d);
            }
        }
        
        return g2d;
    }
}