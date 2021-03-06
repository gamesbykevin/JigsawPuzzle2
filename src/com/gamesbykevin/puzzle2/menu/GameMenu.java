package com.gamesbykevin.puzzle2.menu;

import com.gamesbykevin.puzzle2.main.ResourceManager;
import com.gamesbykevin.puzzle2.main.Main;
import com.gamesbykevin.puzzle2.main.Engine;

import com.gamesbykevin.framework.display.FullScreen;
import com.gamesbykevin.framework.input.*;
import com.gamesbykevin.framework.menu.*;
import com.gamesbykevin.framework.util.*;

import com.gamesbykevin.puzzle2.ai.ArtificialIntelligence;
import com.gamesbykevin.puzzle2.objects.*;
import com.gamesbykevin.puzzle2.shared.Shared;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class GameMenu extends Menu
{
    private boolean fullScreenOn = false, resetGame = true, windowHasFocus = false;
    
    private FullScreen fullScreen;
    
    private Object previousLayerKey;
    
    //object to identify each Option
    public enum OptionKey
    {
        Difficulty, Human, Pieces, NumPlayers, Type, Sound, FullScreen, StartGame, Option, PuzzleCut, 
        Control, Instruction, Credit, 
        GoBack, Resume, NewGame, ExitGame, NewGameConfim, NewGameDeny, ExitGameConfirm, ExitGameDeny
    }
    
    private Option optionNumPlayers, optionType, optionFullScreen, optionSound, optionPieces, optionHuman;
    private Option optionStartGame, optionOptions, optionControls, optionInstructions, optionCredits;
    private Option optionGoBack, optionResume, optionNewGame, optionExitGame, optionDifficulty, optionCut;
    private Option optionNewGameConfirm, optionNewGameDeny, optionExitGameConfirm, optionExitGameDeny;
    
    //object to identify each Layer
    public enum LayerKey 
    {
        GameTitle, Credits, MainTitle, Options, Controls, Instructions1, OptionsInGame, 
        ConfirmNew, Exit, AppletFocus, StartGame, ConfirmNewYes 
    }
    
    private Layer layerGameTitle, layerCredit, layerMainTitle, layerOption;
    private Layer layerControl, layerInstruction1, layerOptionInGame, layerNew, layerExit;
    private Layer layerAppletFocus, layerStartGame, layerConfirmNewYes;
    
    public GameMenu(final ResourceManager resources, final Rectangle screen)
    {
        super(screen);
        
        //setup all options here
        optionDifficulty = new Option("Difficulty: ");
        
        for(ArtificialIntelligence.Difficulty diff : ArtificialIntelligence.Difficulty.values())
        {
            optionDifficulty.add(diff.toString(), resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        }
        
        optionHuman = new Option("1 human player: ");
        optionHuman.add("Yes", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionHuman.add("No",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionNumPlayers = new Option("# of players: ");
        optionNumPlayers.add("1",   resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionNumPlayers.add("2",   resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionNumPlayers.add("4",   resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionNumPlayers.add("6",   resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionNumPlayers.add("9",   resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionNumPlayers.add("12",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionPieces = new Option("# Puzzle Pieces: ");
        optionPieces.add("16",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionPieces.add("25",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionPieces.add("36",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionPieces.add("64",  resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionPieces.add("100", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionPieces.add("225", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionCut = new Option("Puzzle Cut: ");
        
        for(Cutter.PuzzleCut cut: Cutter.PuzzleCut.values())
        {
            optionCut.add(cut.toString(), resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        }
        
        optionType = new Option("Game Type: ");
        optionType.add("Race",        resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionType.add("Time Attack", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionFullScreen = new Option("FullScreen: ");
        optionFullScreen.add("Off",resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionFullScreen.add("On", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionSound = new Option("Sound: ");
        optionSound.add("On", resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        optionSound.add("Off",resources.getMenuAudio(ResourceManager.MenuAudio.MenuChange));
        
        optionStartGame = new Option(LayerKey.StartGame);
        optionStartGame.add("Start Game", null);
        
        optionOptions = new Option(LayerKey.Options);
        optionOptions.add("Options", null);
        
        optionControls = new Option(LayerKey.Controls);
        optionControls.add("Controls", null);
        
        optionInstructions = new Option(LayerKey.Instructions1);
        optionInstructions.add("Instructions", null);
        
        optionCredits = new Option(LayerKey.Credits);
        optionCredits.add("Credits", null);
        
        optionGoBack = new Option(LayerKey.MainTitle);
        optionGoBack.add("Go Back", null);
        
        optionResume = new Option(LayerKey.StartGame);
        optionResume.add("Resume", null);

        optionNewGame = new Option(LayerKey.ConfirmNew);
        optionNewGame.add("New Game", null);
        
        optionExitGame = new Option(LayerKey.Exit);
        optionExitGame.add("Exit Game", null);
        
        optionNewGameConfirm = new Option(LayerKey.ConfirmNewYes);
        optionNewGameConfirm.add("Yes", null);
        
        optionNewGameDeny = new Option(LayerKey.StartGame);
        optionNewGameDeny.add("No", null);
        
        optionExitGameConfirm = new Option(LayerKey.MainTitle);
        optionExitGameConfirm.add("Yes", null);
        
        optionExitGameDeny = new Option(LayerKey.StartGame);
        optionExitGameDeny.add("No", null);
        
        //setup all layers below
        layerGameTitle = new Layer(Layer.Type.NONE, screen);
        layerGameTitle.setImage(resources.getMenuImage(ResourceManager.MenuImage.TitleScreen));
        layerGameTitle.setForce(false);
        layerGameTitle.setPause(false);
        layerGameTitle.setNextLayerKey(LayerKey.Credits);
        layerGameTitle.setTimer(new Timer(TimerCollection.toNanoSeconds(2000L)));
        
        layerCredit = new Layer(Layer.Type.SCROLL_VERTICAL_NORTH, screen);
        layerCredit.setImage(resources.getMenuImage(ResourceManager.MenuImage.Credits));
        layerCredit.setForce(false);
        layerCredit.setPause(false);
        layerCredit.setNextLayerKey(LayerKey.MainTitle);
        layerCredit.setTimer(new Timer(TimerCollection.toNanoSeconds(7500L)));
        
        layerMainTitle = new Layer(Layer.Type.SCROLL_HORIZONTAL_WEST_REPEAT, screen);
        layerMainTitle.setTitle(Shared.GAME_NAME);
        layerMainTitle.setImage(resources.getMenuImage(ResourceManager.MenuImage.TitleBackground));
        layerMainTitle.setForce(false);
        layerMainTitle.setPause(true);
        layerMainTitle.setTimer(new Timer(TimerCollection.toNanoSeconds(30000L)));
        layerMainTitle.add(OptionKey.StartGame,  this.optionStartGame);
        layerMainTitle.add(OptionKey.Option,     this.optionOptions);
        layerMainTitle.add(OptionKey.Control,    this.optionControls);
        layerMainTitle.add(OptionKey.Instruction,this.optionInstructions);
        layerMainTitle.add(OptionKey.Credit,     this.optionCredits);
        
        layerOption = new Layer(Layer.Type.SCROLL_HORIZONTAL_WEST_REPEAT, screen);
        layerOption.setTitle("Options");
        layerOption.setImage(resources.getMenuImage(ResourceManager.MenuImage.TitleBackground));
        layerOption.setForce(false);
        layerOption.setPause(true);
        layerOption.setTimer(new Timer(TimerCollection.toNanoSeconds(30000L)));
        
        layerOption.add(OptionKey.Difficulty, this.optionDifficulty);
        layerOption.add(OptionKey.Sound,      this.optionSound);
        layerOption.add(OptionKey.FullScreen, this.optionFullScreen);
        
        layerOption.add(OptionKey.Difficulty, this.optionDifficulty);
        layerOption.add(OptionKey.Type,       this.optionType);
        layerOption.add(OptionKey.Pieces,     this.optionPieces);
        layerOption.add(OptionKey.PuzzleCut,  this.optionCut);
        layerOption.add(OptionKey.NumPlayers, this.optionNumPlayers);
        layerOption.add(OptionKey.Human,      this.optionHuman);
        
        layerOption.add(OptionKey.GoBack,     this.optionGoBack);
        
        layerControl = new Layer(Layer.Type.NONE, screen);
        layerControl.setImage(resources.getMenuImage(ResourceManager.MenuImage.Controls));
        layerControl.setForce(false);
        layerControl.setPause(true);
        layerControl.setTimer(null);
        layerControl.setNextLayerKey(LayerKey.MainTitle);
        
        layerInstruction1 = new Layer(Layer.Type.NONE, screen);
        layerInstruction1.setImage(resources.getMenuImage(ResourceManager.MenuImage.Instructions1));
        layerInstruction1.setForce(false);
        layerInstruction1.setPause(true);
        layerInstruction1.setTimer(null);
        layerInstruction1.setNextLayerKey(LayerKey.MainTitle);
        
        layerOptionInGame = new Layer(Layer.Type.NONE, screen);
        layerOptionInGame.setTitle("Options");
        layerOptionInGame.setForce(false);
        layerOptionInGame.setPause(true);
        layerOptionInGame.add(OptionKey.Resume,     this.optionResume);
        layerOptionInGame.add(OptionKey.Sound,      this.optionSound);
        layerOptionInGame.add(OptionKey.FullScreen, this.optionFullScreen);
        layerOptionInGame.add(OptionKey.NewGame,    this.optionNewGame);
        layerOptionInGame.add(OptionKey.ExitGame,   this.optionExitGame);
        
        layerNew = new Layer(Layer.Type.NONE, screen);
        layerNew.setTitle("Confirm New");
        layerNew.setForce(false);
        layerNew.setPause(true);
        layerNew.add(OptionKey.NewGameConfim, this.optionNewGameConfirm);
        layerNew.add(OptionKey.NewGameDeny,   this.optionNewGameDeny);
        
        layerExit = new Layer(Layer.Type.NONE, screen);
        layerExit.setTitle("Confirm Exit");
        layerExit.setForce(false);
        layerExit.setPause(true);
        layerExit.add(OptionKey.ExitGameConfirm, this.optionExitGameConfirm);
        layerExit.add(OptionKey.ExitGameDeny,    this.optionExitGameDeny);
        
        layerAppletFocus = new Layer(Layer.Type.NONE, screen);
        layerAppletFocus.setImage(resources.getMenuImage(ResourceManager.MenuImage.AppletFocus));
        layerAppletFocus.setForce(false);
        layerAppletFocus.setPause(true);
        
        layerStartGame = new Layer(Layer.Type.NONE, screen);  //default layer when game is started
        
        layerConfirmNewYes = new Layer(Layer.Type.NONE, screen); //goes to this layer when user wants new game
        
        //add layers to menu here
        add(LayerKey.GameTitle,     layerGameTitle);
        add(LayerKey.Credits,       layerCredit);
        add(LayerKey.MainTitle,     layerMainTitle);
        add(LayerKey.Options,       layerOption);
        add(LayerKey.Controls,      layerControl);
        add(LayerKey.Instructions1, layerInstruction1);
        add(LayerKey.OptionsInGame, layerOptionInGame);
        add(LayerKey.ConfirmNew,    layerNew);
        add(LayerKey.Exit,          layerExit);
        add(LayerKey.AppletFocus,   layerAppletFocus);
        add(LayerKey.StartGame,     layerStartGame);
        add(LayerKey.ConfirmNewYes, layerConfirmNewYes);
        
        setLayer(LayerKey.GameTitle);      //this is the first layer
        setLayerFinish(LayerKey.StartGame);//this is the last layer
    }
    
    public void update(final Main main, final Engine engine, final ResourceManager resources, final Keyboard ki, final Mouse mi) throws Exception
    {
        if (!isMenuFinished())
        {   //if the menu is not on the last layer we need to check for changes made in the menu
            if (isCurrentLayer(LayerKey.MainTitle) && !resetGame)
            {   //if on MainTitle layer recycle gameEngine, but not resources just turn audio off
                resetGame = true;
                resources.stopAllSound();
            }
            
            int valueSound = -1, valueFullScreen = -1;
            
            if (isCurrentLayer(LayerKey.Options))
            {   //if on the options screen check if sound/fullScreen enabled
                valueSound      = getOptionSelectionIndex(LayerKey.Options, OptionKey.Sound);
                valueFullScreen = getOptionSelectionIndex(LayerKey.Options, OptionKey.FullScreen);
            }
            
            if (isCurrentLayer(LayerKey.OptionsInGame))
            {   //if on the in-game options screen check if sound/fullScreen enabled
                valueSound      = getOptionSelectionIndex(LayerKey.OptionsInGame, OptionKey.Sound);
                valueFullScreen = getOptionSelectionIndex(LayerKey.OptionsInGame, OptionKey.FullScreen);
            }
            
            //if starting a new game change layer, stop sound
            if (isCurrentLayer(LayerKey.ConfirmNewYes))
            {
                setLayer(LayerKey.StartGame);
                resetGame = true;
                resources.stopAllSound();
            }
            
            if (valueSound > -1)
            {
                resources.setAudioEnabled(valueSound == 0); //0 is enabled
            }
            
            if (valueFullScreen > -1)
            {
                if (fullScreen == null)
                    fullScreen = new FullScreen();
                
                if (valueFullScreen == 1 && !fullScreenOn || valueFullScreen == 0 && fullScreenOn) //1 is enabled
                {
                    fullScreen.switchFullScreen(main.getApplet(), main.getPanel());
                    fullScreenOn = !fullScreenOn;
                    main.setFullScreen();
                }
            }
            
            if (!windowHasFocus && main.hasFocus() && previousLayerKey != null)
            {   //if the applet has focus and did not previously set menu layer back to cached value
                setLayer(previousLayerKey);
                previousLayerKey = null;
            }

            windowHasFocus = main.hasFocus();

            if (!windowHasFocus && previousLayerKey == null)
            {   //if applet does not have focus and the previous menu layer is not set, set it and send the menu layer to applet focus
                previousLayerKey = getKey();
                setLayer(LayerKey.AppletFocus);
            }
            
            super.update(mi, ki, main.getTimeDeductionPerFrame());
        }
        else
        {   //menu is finished
            if (resetGame)
            {   //if resetGame is enabled and the menu is finished create new instance of GameEngine
                resetGame = false;
                engine.reset();
            }
            
            if (ki.hasKeyPressed(KeyEvent.VK_ESCAPE))
            {   //if in game and Esc is pressed bring in game options menu back up
                setLayer(LayerKey.OptionsInGame);
                ki.resetAllKeyEvents();
            }
        }
    }
    
    public boolean hasFocus()
    {
        return windowHasFocus;
    }
    
    public Graphics render(Graphics g) 
    {
        super.render(g);
        
        return g;
    }
    
    public void dispose()
    {
        super.dispose();
        
        if (fullScreen != null)
            fullScreen.dispose();
        
        fullScreen = null;
        
        previousLayerKey = null;
    }
}