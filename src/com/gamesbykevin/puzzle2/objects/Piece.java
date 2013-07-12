package com.gamesbykevin.puzzle2.objects;

import com.gamesbykevin.framework.base.Sprite;

import java.awt.*;
import java.util.*;

public class Piece extends Sprite
{
    private boolean northMale;
    private boolean southMale;
    private boolean eastMale;
    private boolean westMale;
    
    //as you combine images an image will have child pieces
    private ArrayList childPieces;
    
    //store original width and height
    private int originalWidth, originalHeight;
    
    //we need original position
    private Point originalLocation;
    
    public Piece()
    {
        childPieces = new ArrayList();
    }
    
    public void add(Piece pp)
    {
        //check if puzzle piece being added already has child
        if (pp.hasChild())
        {
            //get children and add to current children piece
            ArrayList children = pp.getChildren();
            for (int i=0; i < children.size(); i++)
            {
                childPieces.add((Piece)children.get(i));
            }
        }
        
        //children added so remove from piece
        pp.resetChild();
        
        //add child to parent
        childPieces.add(pp);
        
        //after child is added align with parent
        setNewPosition(getX(), getY());
    }
    
    public Point getOriginalLocation()
    {
        return this.originalLocation;
    }
    
    public void setOriginalLocation(Point originalLocation)
    {   //set original position
        this.originalLocation = originalLocation;
    }
    
    public void setNewPosition(int x, int y)
    {
        //sets new position for piece and all children
        setLocation(x, y);
        
        for (int i=0; i < childPieces.size(); i++)
        {
            int diffCol = getChild(i).getCol() - getCol();
            int diffRow = getChild(i).getRow() - getRow();
            
            //keep child pieces lined up with parent
            getChild(i).setLocation(x + (diffCol * getOriginalWidth()), y + (diffRow * getOriginalHeight()));
        }
    }
    
    public boolean hasChild()
    {
        return (childPieces.size() > 0);
    }
    
    public boolean hasChild(Point mousePoint)
    {
        for (int i=0; i < childPieces.size(); i++)
        {
            if (getChild(i).getRectangle().contains(mousePoint))
                return true;
        }
        
        return false;
    }
    
    private Piece getChild(int i)
    {
        return (Piece)childPieces.get(i);
    }
    
    public ArrayList getChildren()
    {
        return childPieces;
    }
    
    public void resetChild()
    {
        childPieces.clear();
    }
    
    public void setOriginalWidth(int originalWidth)
    {
        this.originalWidth = originalWidth;
    }
    
    public void setOriginalHeight(int originalHeight)
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
    
    public void setNorthMale(boolean northMale)
    {
        this.northMale = northMale;
    }
    
    public void setSouthMale(boolean southMale)
    {
        this.southMale = southMale;
    }
    
    public void setEastMale(boolean eastMale)
    {
        this.eastMale = eastMale;
    }
    
    public void setWestMale(boolean westMale)
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
    
    public boolean intersectsChild(Piece pp)
    {   //check if any children pieces intersect with puzzle piece or its children
        ArrayList puzzlePieceChildren = pp.getChildren();
        
        if (childPieces.size() > 0)
        {
            for (int i=0; i < childPieces.size(); i++)
            {
                Piece child = getChild(i);

                if (child.intersects(pp))
                    return true;

                for (int x=0; x < puzzlePieceChildren.size(); x++)
                {
                    Piece child2 = (Piece)puzzlePieceChildren.get(x);

                    //check if the current child piece intersects the other puzzle piece's child
                    //or if the other puzzle piece's child intersects current piece
                    if (child2.intersects(child) || child2.intersects(this))
                        return true;
                }
            }
        }
        else
        {   //if the current piece doesnt have child pieces 
            //still check if it intersects the other puzzle piece's child pieces
            for (int x=0; x < puzzlePieceChildren.size(); x++)
            {   //does the current piece intersect the other child pieces
                Piece child2 = (Piece)puzzlePieceChildren.get(x);

                if (child2.intersects(this))
                    return true;
            }
        }
        
        return false;
    }
    
    public boolean intersects(Piece pp)
    {
        //if rectangles dont intersect at all then dont continue test
        if (!pp.getRectangle().intersects(getRectangle()))
            return false;
        
        int verticalH =   (int)(pp.getHeight() * Puzzle.EXTRA_RATIO);
        int horizontalW = (int)(pp.getWidth() *  Puzzle.EXTRA_RATIO);
        
        if (getCol() == pp.getCol() && getRow() - 1 == pp.getRow())
        {   //if two puzzle pieces have the same column and 1 row away
            
            //piece above current get bottom area to test collision
            Rectangle northPieceSouthBorder = new Rectangle(pp.getX(), pp.getY() + pp.getHeight() - verticalH, pp.getWidth(), verticalH);
            
            //current piece get top area to test collision
            Rectangle southPieceNorthBorder = new Rectangle(getX(), getY(), getWidth(), verticalH);
            
            if (southPieceNorthBorder.intersects(northPieceSouthBorder))
                return true;
        }
        
        if (getCol() == pp.getCol() && getRow() + 1 == pp.getRow())
        {   //if two puzzle pieces have the same column and 1 row away
            
            //piece below current piece get top area to test collision
            Rectangle southPieceNorthBorder = new Rectangle(pp.getX(), pp.getY(), pp.getWidth(), verticalH);
            
            //current piece get bottom area to test collision
            Rectangle northPieceSouthBorder = new Rectangle(getX(), getY() + getHeight() - verticalH, getWidth(), verticalH);
            
            if (southPieceNorthBorder.intersects(northPieceSouthBorder))
                return true;
        }
        
        if (getCol() + 1 == pp.getCol() && getRow() == pp.getRow())
        {   //if two puzzle pieces have the same row and 1 column away
            
            //piece to the right of the current piece get left border
            Rectangle eastPieceWestBorder = new Rectangle(pp.getX(), pp.getY(), horizontalW, pp.getHeight());
            
            //current piece get right area to test collision
            Rectangle westPieceEastBorder = new Rectangle(getX() + getWidth() - horizontalW, getY(), horizontalW, getHeight());
            
            if (westPieceEastBorder.intersects(eastPieceWestBorder))
                return true;
        }
        
        if (getCol() - 1 == pp.getCol() && getRow() == pp.getRow())
        {   //if two puzzle pieces have the same row and 1 column away
            
            //piece to the left of the current piece get right border
            Rectangle westPieceEastBorder = new Rectangle(pp.getX() + pp.getWidth() - horizontalW, pp.getY(), horizontalW, pp.getHeight());
            
            //current piece get right area to test collision
            Rectangle eastPieceWestBorder = new Rectangle(getX(), getY(), horizontalW, getHeight());
            
            if (westPieceEastBorder.intersects(eastPieceWestBorder))
                return true;
        }
        
        return false;
    }
    
    public Graphics draw(Graphics g)
    {
        super.draw(g);
        
        for (int i=0; i < childPieces.size(); i++)
        {
            getChild(i).draw(g);
        }
        
        return g;
    }
}