package com.gamesbykevin.puzzle2.objects;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;

public class Cutter 
{
    public enum PuzzleCut
    {
        Traditional, None
    }
    
    public static ArrayList getCutArea(Puzzle pc, Piece pp)
    {
        ArrayList cutAreas = new ArrayList();
        
        int extraW = pc.getExtraWidth();
        int extraH = pc.getExtraHeight();
        
        int cols = pc.getCols();
        int rows = pc.getRows();
        
        //remove 4 corners
        int wc = 0;                             //west side
        int ec = 0 + pp.getWidth() - extraW;    //east side
        int sr = 0 + pp.getHeight() - extraH;   //south side
        int nr = 0;                             //north side
        
        //NW
        cutAreas.add(new Area(new Rectangle(wc, nr, extraW, extraH)));
        //NE
        cutAreas.add(new Area(new Rectangle(ec, nr, extraW, extraH)));
        //SW
        cutAreas.add(new Area(new Rectangle(wc, sr, extraW, extraH)));
        //SE
        cutAreas.add(new Area(new Rectangle(ec, sr, extraW, extraH)));
        
        if (pp.getCol() > 0)
        {
            Area west = new Area(new Rectangle(0, extraH, extraW, pp.getHeight() - (extraH * 2)));
            
            switch(pc.getPuzzleCut())
            {
                case None:
                    break;
                case Traditional:
                    if (pp.hasWestMale())
                    {
                        west.subtract(new Area(new Ellipse2D.Double(0, (pp.getHeight()/2) - (extraH/2), extraW, extraH)));
                    }
                    else
                    {
                        west.add(new Area(new Ellipse2D.Double(extraW, (pp.getHeight()/2) - (extraH/2), extraW, extraH)));
                    }
                    break;
                    
                    
                    //OTHER CUTS CAN GO HERE
                    
            }
            
            cutAreas.add(west);
        }
        
        if (pp.getCol() < cols - 1)
        {
            Area east = new Area(new Rectangle(pp.getWidth() - extraW, extraH, extraW, pp.getHeight() - (extraH * 2)));
            
            switch(pc.getPuzzleCut())
            {
                case None:
                    break;
                case Traditional:
                    if (pp.hasEastMale())
                    {
                        east.subtract(new Area(new Ellipse2D.Double(pp.getWidth() - extraW, (pp.getHeight()/2) - (extraH/2), extraW, extraH)));
                    }
                    else
                    {
                        east.add(new Area(new Ellipse2D.Double(pp.getWidth() - (extraW * 2), (pp.getHeight()/2) - (extraH/2), extraW, extraH)));
                    }
                    break;
            }
            
            cutAreas.add(east);
        }
        
        if (pp.getRow() > 0)
        {
            Area north = new Area(new Rectangle(extraW, 0, pp.getWidth() - (extraW * 2), extraH));
            
            switch(pc.getPuzzleCut())
            {
                case None:
                    break;
                case Traditional:
                    if (pp.hasNorthMale())
                    {
                        north.subtract(new Area(new Ellipse2D.Double((pp.getWidth()/2) - (extraW/2), 0, extraW, extraH)));
                    }
                    else
                    {
                        north.add(new Area(new Ellipse2D.Double((pp.getWidth()/2) - (extraW/2), extraH, extraW, extraH)));
                    }
                    break;
            }
            
            cutAreas.add(north);
        }
        
        if (pp.getRow() < rows - 1)
        {
            Area south = new Area(new Rectangle(extraW, pp.getHeight() - extraH, pp.getWidth() - (extraW * 2), extraH));
            
            switch(pc.getPuzzleCut())
            {
                case None:
                    break;
                case Traditional:
                    if (pp.hasSouthMale())
                    {
                        south.subtract(new Area(new Ellipse2D.Double((pp.getWidth()/2) - (extraW/2), pp.getHeight() - extraH, extraW, extraH)));
                    }
                    else
                    {
                        south.add(new Area(new Ellipse2D.Double((pp.getWidth()/2) - (extraW/2), pp.getHeight() - (extraH*2), extraW, extraH)));
                    }
                    break;
            }
            
            cutAreas.add(south);
        }
        
        return cutAreas;//cutArea
    }
    
    public static Piece createPiece(Puzzle pc, Piece pp) throws Exception
    {
        //get areas to exclude from image
        ArrayList cutAreas = getCutArea(pc, pp);
        
        Rectangle r = pp.getRectangle();
        //grab portion of pixels from original image
        PixelGrabber pg = new PixelGrabber(pc.getImage(), r.x, r.y, r.width, r.height, true);
        pg.grabPixels();
        int pixels[] = (int[])(int[])pg.getPixels();
        
        //creates new image with the given pixels
        MemoryImageSource mis = new MemoryImageSource(r.width, r.height, pixels, 0, r.width);
        Image img = Toolkit.getDefaultToolkit().createImage(mis);
        
        //create image filter that will cut the parts of the image
        CustomImageFilter imgF = new CustomImageFilter(cutAreas);
        
        //apply newly created empty image and apply filter
        FilteredImageSource fis = new FilteredImageSource(img.getSource(), imgF);
        
        //create image from cut image and set the image to the puzzle piece
        pp.setImage(Toolkit.getDefaultToolkit().createImage(fis));
        return pp;
    }
}