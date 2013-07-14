package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.base.Sprite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Piece extends Sprite
{
    private boolean northMale;
    private boolean southMale;
    private boolean eastMale;
    private boolean westMale;
    
    //list of pieces connected to this piece
    private List<Piece> children;
    
    //store original width and height
    private int originalWidth, originalHeight;
    
    //we need original position
    private Point originalLocation;
    
    //the children piece selected
    private int selectedPieceIndex = -1;
    
    public Piece()
    {
        children = new ArrayList<>();
    }
    
    /**
     * Add Piece to current piece. If the new piece has children pieces
     * those children pieces are also added to the current piece.
     * @param newPiece 
     */
    public void add(Piece newPiece)
    {
        //check if puzzle piece being added already has child
        if (newPiece.hasChildren())
        {
            //get children and add to current children piece
            for (Piece piece : newPiece.getChildren())
            {
                children.add(piece);
            }
        }
        
        //children added so remove from new piece
        newPiece.removeChildren();
        
        //add child to parent
        children.add(newPiece);
        
        //after child is added align
        setNewPosition(getPoint());
    }
    
    public Point getOriginalLocation()
    {
        return this.originalLocation;
    }
    
    public void setOriginalLocation(Point originalLocation)
    {   //set original position
        this.originalLocation = originalLocation;
    }
    
    private void resetSelectedPieceIndex()
    {
        this.selectedPieceIndex = -1;
    }
    
    /**
     * Set selected piece whether it be the current piece or the child piece
     */
    public void setSelectedPieceIndex(final Point point)
    {
        resetSelectedPieceIndex();
        
        for (int i=0; i < children.size(); i++)
        {
            if (children.get(i).getRectangle().contains(point))
            {
                this.selectedPieceIndex = i;
                break;
            }
        }
    }
    
    /**
     * Update the puzzle pieces location as well as the children pieces
     * @param Point x,y coordinate
     */
    public void setNewPosition(final Point point)
    {
        point.x -= (getWidth()  / 2);
        point.y -= (getHeight() / 2);
        
        final int baseRow;
        final int baseCol;
        
        if (selectedPieceIndex > -1)
        {
            baseCol = children.get(selectedPieceIndex).getCol();
            baseRow = children.get(selectedPieceIndex).getRow();
        }
        else
        {
            baseCol = getCol();
            baseRow = getRow();
        }
        
        //update parent Piece as well
        int diffCol = getCol() - baseCol;
        int diffRow = getRow() - baseRow;
        super.setLocation(point.x + (diffCol * getOriginalWidth()), point.y + (diffRow * getOriginalHeight()));
        
        for (Piece child : children)
        {
            diffCol = child.getCol() - baseCol;
            diffRow = child.getRow() - baseRow;
            child.setLocation(point.x + (diffCol * getOriginalWidth()), point.y + (diffRow * getOriginalHeight()));
        }
    }
    
    public boolean hasChildren()
    {
        return (children.size() > 0);
    }
    
    public boolean hasChild(final Point point)
    {
        for (int i=0; i < children.size(); i++)
        {
            if (getChild(i).getRectangle().contains(point))
                return true;
        }
        
        return false;
    }
    
    private Piece getChild(final int i)
    {
        return (Piece)children.get(i);
    }
    
    public List<Piece> getChildren()
    {
        return children;
    }
    
    public void removeChildren()
    {
        children.clear();
    }
    
    public void setOriginalWidth(final int originalWidth)
    {
        this.originalWidth = originalWidth;
    }
    
    public void setOriginalHeight(final int originalHeight)
    {
        this.originalHeight = originalHeight;
    }
    
    public int getOriginalWidth()
    {
        return originalWidth;
    }
    
    public int getOriginalHeight()
    {
        return originalHeight;
    }
    
    public void setNorthMale(final boolean northMale)
    {
        this.northMale = northMale;
    }
    
    public void setSouthMale(final boolean southMale)
    {
        this.southMale = southMale;
    }
    
    public void setEastMale(final boolean eastMale)
    {
        this.eastMale = eastMale;
    }
    
    public void setWestMale(final boolean westMale)
    {
        this.westMale = westMale;
    }
    
    public boolean hasNorthMale()
    {
        return northMale;
    }
    
    public boolean hasSouthMale()
    {
        return southMale;
    }
    
    public boolean hasEastMale()
    {
        return eastMale;
    }
    
    public boolean hasWestMale()
    {
        return westMale;
    }
    
    /**
     * Check if any children pieces intersect with puzzle piece or its children
     * @param piece The piece we want to check
     * @return boolean
     */
    public boolean intersectsChild(Piece piece)
    {
        if (children.size() > 0)
        {
            for (int i=0; i < children.size(); i++)
            {
                Piece child = getChild(i);

                if (child.intersects(piece))
                    return true;

                //check if the current child piece intersects the other puzzle piece's child
                //or if the other puzzle piece's child intersects current piece
                for (Piece child2 : piece.getChildren())
                {
                    if (child2.intersects(child) || child2.intersects(this))
                        return true;
                }
            }
        }
        else
        {   
            //if the current piece doesnt have child pieces 
            //still check if it intersects the other puzzle piece's child pieces
            
            //does the current piece intersect the other child pieces
            for (Piece child2 : piece.getChildren())
            {
                if (child2.intersects(this))
                    return true;
            }
        }
        
        //no it doesn't intersect
        return false;
    }
    
    /**
     * Checks if the current piece intersects the piece given
     * @param piece
     * @return boolean
     */
    public boolean intersects(Piece piece)
    {
        //if rectangles don't intersect at all then dont continue test
        if (!piece.getRectangle().intersects(getRectangle()))
            return false;
        
        int verticalH =   (int)(piece.getHeight() * Puzzle.EXTRA_INTERSECT_RATIO);
        int horizontalW = (int)(piece.getWidth() *  Puzzle.EXTRA_INTERSECT_RATIO);
        
        //if two puzzle pieces have the same column and 1 row away
        if (getCol() == piece.getCol() && getRow() - 1 == piece.getRow())
        {
            //piece above current get bottom area to test collision
            Rectangle northPieceSouthBorder = new Rectangle(piece.getX(), piece.getY() + piece.getHeight() - verticalH, piece.getWidth(), verticalH);
            
            //current piece get top area to test collision
            Rectangle southPieceNorthBorder = new Rectangle(getX(), getY(), getWidth(), verticalH);
            
            if (southPieceNorthBorder.intersects(northPieceSouthBorder))
                return true;
        }
        
        //if two puzzle pieces have the same column and 1 row away
        if (getCol() == piece.getCol() && getRow() + 1 == piece.getRow())
        {   
            //piece below current piece get top area to test collision
            Rectangle southPieceNorthBorder = new Rectangle(piece.getX(), piece.getY(), piece.getWidth(), verticalH);
            
            //current piece get bottom area to test collision
            Rectangle northPieceSouthBorder = new Rectangle(getX(), getY() + getHeight() - verticalH, getWidth(), verticalH);
            
            if (southPieceNorthBorder.intersects(northPieceSouthBorder))
                return true;
        }
        
        //if two puzzle pieces have the same row and 1 column away
        if (getCol() + 1 == piece.getCol() && getRow() == piece.getRow())
        {
            //piece to the right of the current piece get left border
            Rectangle eastPieceWestBorder = new Rectangle(piece.getX(), piece.getY(), horizontalW, piece.getHeight());
            
            //current piece get right area to test collision
            Rectangle westPieceEastBorder = new Rectangle(getX() + getWidth() - horizontalW, getY(), horizontalW, getHeight());
            
            if (westPieceEastBorder.intersects(eastPieceWestBorder))
                return true;
        }
        
        if (getCol() - 1 == piece.getCol() && getRow() == piece.getRow())
        {   //if two puzzle pieces have the same row and 1 column away
            
            //piece to the left of the current piece get right border
            Rectangle westPieceEastBorder = new Rectangle(piece.getX() + piece.getWidth() - horizontalW, piece.getY(), horizontalW, piece.getHeight());
            
            //current piece get right area to test collision
            Rectangle eastPieceWestBorder = new Rectangle(getX(), getY(), horizontalW, getHeight());
            
            if (westPieceEastBorder.intersects(eastPieceWestBorder))
                return true;
        }
        
        return false;
    }
    
    @Override
    public Graphics draw(Graphics g)
    {
        super.draw(g);
        
        for (int i=0; i < children.size(); i++)
        {
            getChild(i).draw(g);
        }
        
        return g;
    }
}