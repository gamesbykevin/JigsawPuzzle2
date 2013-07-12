package com.gamesbykevin.puzzle2.main;

import com.gamesbykevin.framework.resources.*;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.text.MessageFormat;
import java.util.LinkedHashMap;

/**
 * This class will load all resources and provide ways to access them
 * @author GOD
 */
public class ResourceManager 
{   
    //this will contain all resources
    private LinkedHashMap everyResource = new LinkedHashMap();
    
    //collections of resources
    private enum Type
    {
        MenuImage, MenuAudio, GameImage, GameFont, GameAudio
    }
    
    //root directory of all resources
    public static final String RESOURCE_DIR = "resources/"; 
    
    public enum MenuAudio
    {
        MenuChange
    }
    
    public enum MenuImage
    {
        TitleScreen, Credits, AppletFocus, TitleBackground, Mouse, MouseDrag, Instructions1, Controls
    }
    
    public enum GameImage
    {
        Pic1, Pic2, Pic3, Pic4, Pic5, Pic6, Pic7, Pic8, Pic9, Pic10, 
        Pic11, Pic12, Pic13, Pic14, Pic15, Pic16, Pic17, Pic18, Pic19, Pic20
    }
    
    public enum GameFont
    {
        Dialog
    }
    
    public enum GameAudio
    {
        Mark
    }
    
    //indicates wether or not we are still loading resources
    private boolean loading = true;
    
    public ResourceManager()
    {
        //load all menu images
        add(Type.MenuImage, (Object[])MenuImage.values(), RESOURCE_DIR + "images/menu/{0}.gif", "Loading Menu Image Resources", Resources.Type.Image);
        
        //load all game images
        add(Type.GameImage, (Object[])GameImage.values(), RESOURCE_DIR + "images/game/{0}.jpg", "Loading Game Image Resources", Resources.Type.Image);
        
        //load all game fonts
        add(Type.GameFont, (Object[])GameFont.values(), RESOURCE_DIR + "font/{0}.ttf", "Loading Game Font Resources", Resources.Type.Font);
        
        //load all menu audio
        add(Type.MenuAudio, (Object[])MenuAudio.values(), RESOURCE_DIR + "audio/menu/{0}.wav", "Loading Menu Audio Resources", Resources.Type.Audio);
        
        //load all game audio
        add(Type.GameAudio, (Object[])GameAudio.values(), RESOURCE_DIR + "audio/game/{0}.wav", "Loading Game Audio Resources", Resources.Type.Audio);
    }
    
    //add a collection of resources audio/image/font/text
    private void add(final Object key, final Object[] eachResourceKey, final String directory, final String loadDesc, final Resources.Type resourceType)
    {
        String[] locations = new String[eachResourceKey.length];
        for (int i=0; i < locations.length; i++)
        {
            locations[i] = MessageFormat.format(directory, i);
        }

        Resources resources = new Resources(Resources.LoadMethod.OnePerFrame, locations, eachResourceKey, resourceType);
        resources.setDesc(loadDesc);
        
        everyResource.put(key, resources);
    }
    
    public boolean isLoading()
    {
        return loading;
    }
    
    private Resources getResources(Object key)
    {
        return (Resources)everyResource.get(key);
    }
    
    public Font getGameFont(Object key)
    {
        return getResources(Type.GameFont).getFont(key);
    }
    
    public Image getGameImage()
    {
        return getResources(Type.GameImage).getImage(GameImage.values()[(int)(Math.random() * GameImage.values().length)]);
    }
    
    public Image getGameImage(Object key)
    {
        return getResources(Type.GameImage).getImage(key);
    }
    
    public Image getMenuImage(Object key)
    {
        return getResources(Type.MenuImage).getImage(key);
    }
    
    public AudioResource getMenuAudio(Object key)
    {
        return getResources(Type.MenuAudio).getAudio(key);
    }
    
    public void playSound(Object key, boolean loop)
    {
        getResources(Type.GameAudio).playAudio(key, loop);
    }
    
    public void stopSound(Object key)
    {
        getResources(Type.GameAudio).getAudio(key).stop();
    }
    
    public void stopAllSound()
    {
        getResources(Type.GameAudio).stopAllAudio();
    }
    
    public void update(final Class source) 
    {
        Object[] keys = everyResource.keySet().toArray();
        
        for (Object key : keys)
        {
            Resources r = getResources(key);
            
            if (!r.isLoadingComplete())
            {
                r.loadResources(source);
                return;
            }
        }

        //if this line is reached we are done loading every resource
        loading = false;
    }
    
    public boolean isAudioEnabled()
    {
        return getResources(Type.GameAudio).isAudioEnabled();
    }
    
    public void setAudioEnabled(boolean soundEnabled)
    {
        Resources r = getResources(Type.GameAudio);
        
        if (r.isAudioEnabled() && soundEnabled)
            return;
        
        if (!r.isAudioEnabled() && !soundEnabled)
            return;
        
        r.setAudioEnabled(soundEnabled);
        
        if (!soundEnabled)
        {
            if (r != null)
                r.stopAllAudio();
        }
    }
    
    public void dispose()
    {
        Object[] keys = everyResource.keySet().toArray();
        
        for (Object key : keys)
        {
            Resources r = getResources(key);
            
            if (r != null)
                r.dispose();
            
            r = null;
            
            everyResource.put(key, r);
        }
        
        everyResource.clear();
        everyResource = null;
    }
    
    public Graphics draw(Graphics g, final Rectangle screen)
    {
        if (!loading)
            return g;
        
        Object[] keys = everyResource.keySet().toArray();
        
        for (Object key : keys)
        {
            Resources r = getResources(key);
            
            if (!r.isLoadingComplete())
            {
                Progress.draw(g, screen, r.getProgress(), r.getDesc());
                return g;
            }
        }
        
        return g;
    }
}