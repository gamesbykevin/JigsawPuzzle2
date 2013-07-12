package com.gamesbykevin.puzzle2.objects;

import java.awt.geom.Area;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;

public class CustomImageFilter extends RGBImageFilter
{
    //any (x,y) that falls into these areas will not be included in image
    private ArrayList cutAreas;

    public CustomImageFilter(ArrayList cutAreas)
    {
        this.cutAreas = cutAreas;
    }

    public int filterRGB(int x, int y, int rgb)
    {
        for (int i=0 ; i < cutAreas.size(); i++)
        {
            Area tmp = (Area)cutAreas.get(i);
            
            if (tmp.contains(x, y))
                return getEmptyRGB(rgb);
        }
        
        return rgb;
    }

    private int getEmptyRGB(int rgb)
    {
        return (0xffffff & rgb);
    }
}