package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.input.Mouse;
import com.gamesbykevin.framework.resources.Progress;
import com.gamesbykevin.framework.util.TimerCollection;

import com.gamesbykevin.puzzle2.ai.*;

import java.awt.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Puzzle 
{
    private ArrayList pieces;
    private Image image;    //entire image before cut
    
    //this is the extra width and height on each puzzle piece
    private int extraW, extraH;
    
    //entire puzzle dimensions
    private int puzzleWidth, puzzleHeight;
    
    //number of cols/rows for this picture puzzle
    private int rows, cols;
    
    //use progress tracker to track images loading
    private Progress cuttingProgress;
    
    //use progress to track scrabling images
    private Progress scramblingProgress;
    
    //current puzzle piece selected
    private int currentPiece = -1;
    
    //this is the ratio of the original width to extend the image on each end
    public static final double EXTRA_RATIO = .25;
    
    private Rectangle screen;
    
    //should the computer autoSolve the puzzle
    private boolean autoSolve = false;
    
    //computer element that solves the puzzle
    private ArtificialIntelligence ai;
    
    //how much time has passed
    private TimerCollection timers;
    
    //difficulty and game type
    private int difficultyIndex;
    private int gameTypeIndex;
    
    //is game over
    private boolean gameOver = false;
    
    //what place did this object come in
    private int place = -1;
    
    //format to display timer
    private static final String TIME_FORMAT = "mm:ss.SSS";
    
    public enum TimerTrackers
    {
        GameTimer, CpuMoveTimer
    }
    
    private Cutter.PuzzleCut puzzleCut;
    
    public Puzzle(Image image, int rows, int cols, Rectangle screen, long timeDeduction, int gameTypeIndex, int difficultyIndex, int puzzleCutIndex)
    {
        this.puzzleCut = Cutter.PuzzleCut.values()[puzzleCutIndex];
        this.screen = screen;
        this.difficultyIndex = difficultyIndex;
        this.gameTypeIndex = gameTypeIndex;
        
        if (image.getWidth(null) >= screen.width || image.getHeight(null) >= screen.height)
        {
            //if image is bigger than the window resize image 
            //assume puzzle image has same width and height
            int maxDim = screen.width;
            
            if (screen.width > screen.height)
                maxDim = screen.height;
            
            //puzzle image will be 75% of screen size
            int bothDim = (int)(maxDim * .75);
            
            BufferedImage resizedImage = new BufferedImage(bothDim, bothDim, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(image, 0, 0, bothDim, bothDim, null);
            //g.drawImage(image, 0, 0, screen.width, screen.height, null);
            g.dispose();
            
            this.image = resizedImage;
        }
        else
        {
            this.image = image;
        }
        
        timers = new TimerCollection(timeDeduction);
        
        if (gameTypeIndex == 1)
        {   //time attack
            long reset = ArtificialIntelligence.getDifficultyDelay(ArtificialIntelligence.Difficulty.values()[difficultyIndex]);
            reset *= (rows * cols);
            timers.add(TimerTrackers.GameTimer, reset);
        }
        else
        {   //race
            timers.add(TimerTrackers.GameTimer);
        }
        
        timers.add(TimerTrackers.CpuMoveTimer);
        
        pieces = new ArrayList();
        
        //width of each puzzle piece
        int width  = (int)(this.image.getWidth(null) / cols);
        
        //height of each puzzle piece
        int height = (int)(this.image.getHeight(null) / rows);
        
        //entire puzzle width
        puzzleWidth = this.image.getWidth(null);
        
        //entire puzzle height
        puzzleHeight = this.image.getHeight(null);
        
        //calculate extra width and height
        this.extraW = (int)(width * EXTRA_RATIO);
        this.extraH = (int)(height * EXTRA_RATIO);
        
        //set number of rows/cols
        this.rows = rows;
        this.cols = cols;
        
        //create new progress tracker
        cuttingProgress = new Progress(rows * cols);
        scramblingProgress = new Progress(rows * cols);
        
        for (int col=0; col < cols; col++)
        {
            for (int row=0; row < rows; row++)
            {
                int x = (col * width); //  + screen.x;
                int y = (row * height);// + screen.y;
                
                int totalWidth  = width  + (extraW * 2);
                int totalHeight = height + (extraH * 2);
                
                Rectangle puzzlePieceArea = new Rectangle(x - extraW, y - extraH, totalWidth, totalHeight);
                
                Piece piece = new Piece();
                piece.setCol(col);
                piece.setRow(row);
                piece.setLocation(puzzlePieceArea.x, puzzlePieceArea.y);
                piece.setDimensions(puzzlePieceArea.width, puzzlePieceArea.height);
                
                //set original dimensions for later when we keep connected pieces aligned
                piece.setOriginalWidth(width);
                piece.setOriginalHeight(height);
                
                Piece ppNorth = getPiece(col, row - 1);
                Piece ppSouth = getPiece(col, row + 1);
                Piece ppEast = getPiece(col + 1, row);
                Piece ppWest = getPiece(col - 1, row);
                
                if (ppNorth != null)
                {
                    piece.setNorthMale(!ppNorth.hasSouthMale());
                }
                else
                {
                    if (row > 0) //if not the top row and north is not set
                        piece.setNorthMale(Math.random() > .5);
                }
                
                if (ppSouth != null)
                {
                    piece.setSouthMale(!ppSouth.hasNorthMale());
                }
                else
                {
                    if (row < rows - 1) //if not the bottom row and south is not set
                        piece.setSouthMale(Math.random() > .5);
                }
                
                if (ppEast != null)
                {
                    piece.setEastMale(!ppEast.hasWestMale());
                }
                else
                {
                    if (col < cols - 1) //if not the last col and east is not set
                        piece.setEastMale(Math.random() > .5);
                }
                
                if (ppWest != null)
                {
                    piece.setWestMale(!ppWest.hasEastMale());
                }
                else
                {
                    if (col > 0) //if not the first col and west is not set
                        piece.setWestMale(Math.random() > .5);
                }
                
                pieces.add(piece);
            }
        }
    }
    
    public Cutter.PuzzleCut getPuzzleCut()
    {
        return puzzleCut;
    }
    
    public int getExtraWidth()
    {
        return extraW;
    }
    
    public int getExtraHeight()
    {
        return extraH;
    }
    
    public int getCols()
    {
        return cols;
    }
    
    public int getRows()
    {
        return rows;
    }
    
    public void setAutoSolve(boolean autoSolve)
    {
        this.autoSolve = autoSolve;
    }
    
    public boolean hasAutoSolve()
    {
        return this.autoSolve;
    }
    
    public Image getImage()
    {
        return image;
    }
    
    public boolean isCuttingComplete()
    {
        return cuttingProgress.isLoadingComplete();
    }
    
    public boolean isScramblingComplete()
    {
        return scramblingProgress.isLoadingComplete();
    }
    
    public void update(Mouse mouse) throws Exception
    {
        if (!isCuttingComplete())
        {
            //get puzzle piece
            Piece pp = getPiece(cuttingProgress.getCurrentCount());

            //cut image for puzzle piece
            pp = Cutter.createPiece(this, pp);

            //set puzzle piece after cut image has been set
            setPiece(cuttingProgress.getCurrentCount(), pp);

            cuttingProgress.increaseProgress();
        }
        else
        {
            if (!isScramblingComplete())
            {
                Piece pp = getPiece(scramblingProgress.getCurrentCount());
                
                int randX = screen.x + (int)(Math.random() * (screen.width  - pp.getWidth() ));
                int randY = screen.y + (int)(Math.random() * (screen.height - pp.getHeight()));
                
                pp.setLocation(randX, randY);
                pp.setOriginalLocation(pp.getPoint());//set original location for
                
                scramblingProgress.increaseProgress();
            }
            else
            {
                if (!hasGameOver())
                {
                    timers.update();
                    
                    if (gameTypeIndex == 1)
                    {   //if time attack mode make sure timer doesnt go negative
                        if (timers.hasTimePassed(TimerTrackers.GameTimer))
                        {
                            timers.setRemaining(TimerTrackers.GameTimer, 0);
                            setGameOver(true);
                            return;
                        }
                    }
                    
                    if (hasAutoSolve())
                    {
                        if (ai == null)
                            ai = new ArtificialIntelligence(this, ArtificialIntelligence.Difficulty.values()[difficultyIndex]);

                        ai.solve(this);
                    }
                    else
                    {
                        //creating-scrambling images complete now check mouse input
                        if (!hasSelectedPiece())
                        {   //if no puzzle piece is selected check mouse actions
                            if (mouse.isMouseDragged() || mouse.isMouseClicked())
                            {
                                setSelectedPiece(mouse.getMouseLocation());
                            }
                        }
                        else
                        {
                            if (mouse.isMouseDragged())
                            {   //if dragging mouse move puzzle piece appropriately
                                setPieceLocation(mouse.getMouseLocation());
                            }

                            if (mouse.isMouseReleased())
                            {   //if mouse released reset current puzzle piece selected
                                checkMatch();
                                resetSelectedPiece();
                                hasGameOver();
                            }
                        }
                    }
                }
            }
        }
    }
    
    public float getProgress()
    {
        double total = rows * cols;
        double progress = total - (pieces.size() - 1);
        
        if (progress < 0)
            progress = 0;
        
        return (float)(progress / total);
    }
    
    public Point getDestination(Piece pp)
    {
        int x = screen.x + (screen.width  / 2) - (puzzleWidth  / 2) + (pp.getCol() * pp.getOriginalWidth());
        int y = screen.y + (screen.height / 2) - (puzzleHeight / 2) + (pp.getRow() * pp.getOriginalHeight());

        return new Point(x, y);
    }
    
    public void setGameOver(boolean gameOver)
    {
        this.gameOver = gameOver;
    }
    
    public boolean hasGameOver()
    {   //there is now 1 piece left so puzzle is complete
        if (!gameOver)
            gameOver = (pieces.size() == 1);
        
        return gameOver;
    }
    
    public boolean hasPlace()
    {
        return (place > 0);
    }
    
    public void setPlace(int place)
    {
        this.place = place;
    }
    
    private void setPieceLocation(Point mousePoint)
    {
        Piece pp = getCurrentPiece();
        pp.setNewPosition(mousePoint.x - (pp.getWidth()/2), mousePoint.y - (pp.getHeight()/2));
    }
    
    public TimerCollection getTimerCollection()
    {
        return timers;
    }
    
    private void checkMatch()
    {
        if (hasSelectedPiece())
        {
            Piece pp = getCurrentPiece();
            
            for (int i=0; i < pieces.size(); i++)
            {
                if (i == getSelectedPiece())
                    continue;
                
                Piece tmp = getPiece(i);
                
                //check when adding current piece to an existing piece that has child for match
                if (pp.intersects(tmp) || pp.intersectsChild(tmp))
                {   //if the piece selected matches we need to combine the two together
                    //add piece mathcing to child
                    pp.add(tmp);
                    setPiece(getSelectedPiece(), pp);
                    
                    //remove puzzle piece after adding as a child
                    removePiece(i);
                    
                    //selected piece has been matched exit loop
                    break;
                }
            }
        }
    }
    
    public ArrayList getPieces()
    {
        return pieces;
    }
    
    public void removePiece(Piece pp)
    {
        for (int i=0; i < pieces.size(); i++)
        {
            Piece tmp = getPiece(i);
            
            if (tmp.getCol() == pp.getCol() && tmp.getRow() == pp.getRow())
            {
                removePiece(i);
                break;
            }
        }        
    }
    
    public void removePiece(int i)
    {
        pieces.remove(i);
    }
    
    public void mergePieces()
    {
        for (int i=0; i < pieces.size(); i++)
        {
            Piece p1 = getPiece(i);
            
            boolean exit = false;
            
            for (int x=0; x < pieces.size(); x++)
            {
                if (i == x)
                    continue;
                
                Piece p2 = getPiece(x);
                
                if (p1.intersects(p2) || p1.intersectsChild(p2))
                {
                    //add to child
                    p1.add(p2);
                    setPiece(i, p1);
                    
                    //remove piece since it has been added as child
                    removePiece(x);
                    x--;
                    
                    //loop through existing pieces for any other merges
                    exit = true;
                }
            }
            
            if (exit)   //if merges were found exit
                break;
        }
    }
    
    public void resetSelectedPiece()
    {
        currentPiece = -1;
    }
    
    public boolean hasSelectedPiece()
    {
        return (currentPiece > -1);
    }
    
    public int getSelectedPiece()
    {
        return this.currentPiece;
    }
    
    public void setSelectedPiece(int currentPiece)
    {
        this.currentPiece = currentPiece;
    }
    
    private void setSelectedPiece(Point mousePoint)
    {
        for (int i=pieces.size() - 1; i >= 0; i--)
        {
            Piece pp = getPiece(i);
            
            if (pp.getRectangle().contains(mousePoint) || pp.hasChild(mousePoint))
            {
                setSelectedPiece(i);
                break;
            }
        }
    }
    
    private Piece getPiece(int col, int row)
    {
        for (int i=0; i < pieces.size(); i++)
        {
            Piece pp = getPiece(i);
            
            if (pp.equals(col, row))
                return pp;
        }
        
        return null;
    }
    
    public Piece getPiece(int i)
    {
        return (Piece)pieces.get(i);
    }
    
    public Piece getCurrentPiece()
    {
        return getPiece(currentPiece);
    }
    
    public void setPiece(int i, Piece pp)
    {
        pieces.set(i, pp);
    }
    
    public Graphics draw(Graphics g)
    {
        if (!isCuttingComplete())
        {
            Progress.draw(g, screen, cuttingProgress.getProgress(), "Cutting Images");
        }
        else
        {
            if (!isScramblingComplete())
            {
                Progress.draw(g, screen, scramblingProgress.getProgress(), "Scrambling Images");
            }
            else
            {
                for (int i=0; i < pieces.size(); i++)
                {
                    getPiece(i).draw(g);
                }
                
                if (hasSelectedPiece())
                {   //draw selected piece last so it appears on top of others
                    getCurrentPiece().draw(g);
                }
                
                drawPuzzleProgress(g);
            }
        }
        
        return g;
    }
    
    private Graphics drawPuzzleProgress(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        Stroke defaultStroke = g2d.getStroke();
        BasicStroke stroke = new BasicStroke(6.0f);
        g2d.setStroke(stroke);
        
        g2d.setColor(Color.white);
        g2d.drawRect(screen.x, screen.y, screen.width, screen.height);
        
        String desc = "";
        
            if (hasGameOver() && hasPlace())
                desc += "#" + place;
            
            if (hasAutoSolve())
            {
                desc += " Cpu";
                g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 12));
            }
            else
            {
                g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 16));
                desc += " Human ";
                
                //race
                if (gameTypeIndex == 0)
                {
                    desc += timers.getTimer(TimerTrackers.GameTimer).getDescPassed(TIME_FORMAT);
                }
                else
                {
                    //time attack
                    desc += timers.getTimer(TimerTrackers.GameTimer).getDescRemaining(TIME_FORMAT);
                }
            }
        
        int x = screen.x + (screen.width/2) - (g2d.getFontMetrics().stringWidth(desc)/2);
        g2d.drawString(desc, x, screen.y + (int)(g2d.getFontMetrics().getHeight() * 1.5));
        g2d.setStroke(defaultStroke);
        return (Graphics)g2d;
    }
}