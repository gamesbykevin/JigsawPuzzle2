package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.input.Mouse;
import com.gamesbykevin.framework.resources.Progress;
import com.gamesbykevin.framework.util.TimerCollection;

import com.gamesbykevin.puzzle2.ai.*;

import java.awt.*;
import java.awt.image.*;
import java.util.List;
import java.util.ArrayList;

public class Puzzle 
{
    private List<Piece> pieces;
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
    private int currentPieceIndex = -1;
    
    //this is the ratio of the original width to extend the image on each end
    public static final double EXTRA_RATIO = .25;
    
    //this is the ratio on the end
    public static final double EXTRA_INTERSECT_RATIO = .25;
    
    //should the computer autoSolve the puzzle
    private boolean autoSolve = false;
    
    //computer element that solves the puzzle
    private ArtificialIntelligence ai;
    
    //how much time has passed
    private TimerCollection timers;
    
    //difficulty and game type
    private int difficultyIndex;
    private int gameTypeIndex;
    
    //the window this puzzle is contained within
    private final Rectangle screen;
    
    //is game over
    private boolean gameOver = false;
    
    //what place did this object come in
    private int place = -1;
    
    public enum TimerTrackers
    {
        GameTimer, CpuMoveTimer
    }
    
    private Cutter.PuzzleCut puzzleCut;
    
    public Puzzle(Image image, int rows, int cols, Rectangle screen, long timeDeduction, int gameTypeIndex, int difficultyIndex, int puzzleCutIndex)
    {
        this.screen = screen;
        this.puzzleCut = Cutter.PuzzleCut.values()[puzzleCutIndex];
        this.difficultyIndex = difficultyIndex;
        this.gameTypeIndex = gameTypeIndex;
        
        //if image is bigger than the window resize image 
        if (image.getWidth(null) >= screen.width || image.getHeight(null) >= screen.height)
        {
            //assume puzzle image has same width and height
            int maxDim = screen.width;
            
            if (screen.width > screen.height)
                maxDim = screen.height;
            
            //puzzle image will be 75% of screen size
            int bothDim = (int)(maxDim * .75);
            
            BufferedImage resizedImage = new BufferedImage(bothDim, bothDim, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(image, 0, 0, bothDim, bothDim, null);
            g.dispose();
            
            this.image = resizedImage;
        }
        else
        {
            //image was not bigger so resize is not needed
            this.image = image;
        }
        
        timers = new TimerCollection(timeDeduction);
        
        //time attack
        if (gameTypeIndex == 1)
        {
            long reset = ArtificialIntelligence.getDifficultyDelay(ArtificialIntelligence.Difficulty.values()[difficultyIndex]);
            reset *= (rows * cols);
            timers.add(TimerTrackers.GameTimer, reset);
        }
        else
        {
            //race
            timers.add(TimerTrackers.GameTimer);
        }
        
        timers.add(TimerTrackers.CpuMoveTimer);
        
        pieces = new ArrayList<>();
        
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
            Piece piece = pieces.get(cuttingProgress.getCurrentCount());

            //cut image for puzzle piece
            piece = Cutter.createPiece(this, piece);

            //set puzzle piece after cut image has been set
            setPiece(cuttingProgress.getCurrentCount(), piece);

            cuttingProgress.increaseProgress();
        }
        else
        {
            if (!isScramblingComplete())
            {
                Piece piece = pieces.get(scramblingProgress.getCurrentCount());
                
                int randX = screen.x + (int)(Math.random() * (screen.width  - piece.getWidth() ));
                int randY = screen.y + (int)(Math.random() * (screen.height - piece.getHeight()));
                
                piece.setLocation(randX, randY);
                piece.setOriginalLocation(piece.getPoint());//set original location for
                
                scramblingProgress.increaseProgress();
            }
            else
            {
                if (!hasGameOver())
                {
                    timers.update();
                    
                    //if time attack mode make sure timer doesnt go negative
                    if (gameTypeIndex == 1)
                    {
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
                                setSelectedPiece(mouse.getLocation());
                        }
                        else
                        {
                            //if dragging mouse move puzzle piece appropriately
                            if (mouse.isMouseDragged())
                            {
                                getPiece().setNewPosition(mouse.getLocation());
                            }

                            //if mouse released reset current puzzle piece selected
                            if (mouse.isMouseReleased())
                            {
                                checkMatch();
                                resetSelectedPieceIndex();
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
    
    public Point getDestination(Piece piece)
    {
        int x = screen.x + (screen.width  / 2) - (puzzleWidth  / 2) + (piece.getCol() * piece.getOriginalWidth());
        int y = screen.y + (screen.height / 2) - (puzzleHeight / 2) + (piece.getRow() * piece.getOriginalHeight());

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
    
    public TimerCollection getTimerCollection()
    {
        return timers;
    }
    
    private void checkMatch()
    {
        //get the current selected piece
        Piece piece = getPiece();

        for (int i=0; i < pieces.size(); i++)
        {
            if (i == getSelectedPieceIndex())
                continue;

            //get piece we want to test
            Piece tmp = pieces.get(i);

            //if the piece selected matches we need to combine the two together
            if (piece.intersects(tmp) || piece.intersectsChild(tmp))
            {
                //add piece matching to child
                piece.add(tmp);
                setPiece(getSelectedPieceIndex(), piece);

                //remove puzzle piece after adding as a child
                removePiece(i);

                //selected piece has been matched exit loop
                break;
            }
        }
    }
    
    public List<Piece> getPieces()
    {
        return pieces;
    }
    
    public void removePiece(Piece pp)
    {
        for (int i=0; i < pieces.size(); i++)
        {
            Piece tmp = pieces.get(i);
            
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
            Piece p1 = pieces.get(i);
            
            boolean exit = false;
            
            for (int x=0; x < pieces.size(); x++)
            {
                if (i == x)
                    continue;
                
                Piece p2 = pieces.get(x);
                
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
    
    public void resetSelectedPieceIndex()
    {
        setSelectedPieceIndex(-1);
    }
    
    public boolean hasSelectedPiece()
    {
        return (getSelectedPieceIndex() > -1);
    }
    
    public int getSelectedPieceIndex()
    {
        return this.currentPieceIndex;
    }
    
    /**
     * Sets the index in our pieces list.
     * @param currentPieceIndex 
     */
    public void setSelectedPieceIndex(final int currentPieceIndex)
    {
        this.currentPieceIndex = currentPieceIndex;
    }
    
    /**
     * Set the selected piece based on the mouse location.
     * If multiple pieces are at the same location
     * the piece drawn last on the screen will be the piece selected.
     * @param mousePoint x,y coordinate location of mouse
     */
    private void setSelectedPiece(final Point mousePoint)
    {
        //start witht the last piece in the collection because that will be the piece drawn on top
        for (int i = pieces.size() - 1; i >= 0; i--)
        {
            Piece piece = pieces.get(i);
            
            if (piece.getRectangle().contains(mousePoint) || piece.hasChild(mousePoint))
            {
                //set the index of the selected piece
                setSelectedPieceIndex(i);
                
                //set the index of the child piece if a child piece was selected
                piece.setSelectedPieceIndex(mousePoint);
                break;
            }
        }
    }
    
    /**
     * Returns the puzzle piece at the given col, row
     * @param col
     * @param row
     * @return Piece
     */
    private Piece getPiece(final int col, final int row)
    {
        for (Piece piece : pieces)
        {
            if (piece.equals(col, row))
                return piece;
        }
        
        return null;
    }
    
    /**
     * Returns the current piece selected, null if no object
     * @return Piece
     */
    public Piece getPiece()
    {
        if (getSelectedPieceIndex() >= 0)
            return pieces.get(getSelectedPieceIndex());
        else
            return null;
    }
    
    public void setPiece(final int i, final Piece piece)
    {
        pieces.set(i, piece);
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
                for (Piece piece : pieces)
                {
                    piece.draw(g);
                }
                
                //draw selected piece last so it appears on top of others
                if (hasSelectedPiece())
                {
                    getPiece().draw(g);
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
                    desc += timers.getTimer(TimerTrackers.GameTimer).getDescPassed(TimerCollection.FORMAT_6);
                }
                else
                {
                    //time attack
                    desc += timers.getTimer(TimerTrackers.GameTimer).getDescRemaining(TimerCollection.FORMAT_6);
                }
            }
        
        int x = screen.x + (screen.width/2) - (g2d.getFontMetrics().stringWidth(desc)/2);
        g2d.drawString(desc, x, screen.y + (int)(g2d.getFontMetrics().getHeight() * 1.5));
        g2d.setStroke(defaultStroke);
        return (Graphics)g2d;
    }
}