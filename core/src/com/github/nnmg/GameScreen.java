package com.github.nnmg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class GameScreen implements Screen {
    private final Main game;

    private SpriteBatch batch;
    private Texture walkSheet;
    private Texture enemyImage;
    private Texture npcImage;
    private Music theme;
    private Sound hit;
    private OrthographicCamera camera;
    private Rectangle player;
    private Array<Rectangle> enemies;
    private Array<Rectangle> talkers;
    private int one;
    private int two;
    private TextButton tb;
    private boolean isTalking;
    private boolean talk;
    private boolean talkHelper;
    private TiledMapRenderer tiledMapRenderer;
    private Preferences prefs;
    private static final int FRAME_COLS=8, FRAME_ROWS=4;
    private Animation<TextureRegion> downAnim;
    private Animation<TextureRegion> upAnim;
    private Animation<TextureRegion> leftAnim;
    private Animation<TextureRegion> rightAnim;
    private Animation<TextureRegion>  swordAnims;
    private Texture stayStill;
    private Texture tree;
    private Texture sword;
    private boolean slash;
    private float stateTime;
    private float stateTime1;
    private int push;

    @SuppressWarnings("GwtInconsistentSerializableClass")
    public enum NPC{
        BOB("Bob", new Texture(Gdx.files.internal("npc.png")), "Wow, lots of monsters out here.");
        String name;
        String dialogue;
        Texture image;
        NPC(String name, Texture image, String dialogue) {
            this.name=name;
            this.dialogue=dialogue;
            this.image=image;

        }

        public String getDialogue() {
            return dialogue;
        }

        public String getName() {
            return name;
        }

        public Texture getImage() {
            return image;
        }
    }
    NPC bob;
    public GameScreen (final Main game) {
        //Will be used to count seconds (for autosave)
        push=0;
        bob=NPC.BOB;
        //Two booleans used to control NPC talking
        talkHelper=true;
        talk=false;
        slash=false;
        //Preferences, used to save the game
        prefs=Gdx.app.getPreferences("My Preferences");
        talkers=new Array<Rectangle>();
        //Instantiate images. Walk is a spritesheet
        walkSheet=new Texture(Gdx.files.internal("walk.png"));
        sword=new Texture(Gdx.files.internal("sword.png"));
        stayStill=new Texture(Gdx.files.internal("static.png"));
        //Spritesheet split up into 8x4
        TextureRegion[][] walkRegion=TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAME_COLS, walkSheet.getHeight()/FRAME_ROWS);
        TextureRegion[][] swordRegion=TextureRegion.split(sword, sword.getWidth()/5, sword.getHeight());
        //Further split up by rows into up, down, left, and right
        TextureRegion[] walkDown=new TextureRegion[FRAME_COLS];
        TextureRegion[] walkUp=new TextureRegion[FRAME_COLS];
        TextureRegion[] walkLeft=new TextureRegion[FRAME_COLS];
        TextureRegion[] walkRight=new TextureRegion[FRAME_COLS];
        TextureRegion[] swordAnim=new TextureRegion[5];
        int indexS=0;
        for(int i=0;i<5;i++)
        {
            swordAnim[indexS++]=swordRegion[0][i];
        }
        //Add each individual sprite from each row into an array for animating (Don't worry about this)
        int indexD=0;
        int indexU=0;
        int indexR=0;
        int indexL=0;
        tree=new Texture(Gdx.files.internal("tree.png"));
        for(int i=0; i<1; i++){
            for(int j=0; j<FRAME_COLS; j++){
                walkDown[indexD++]=walkRegion[i][j];
            }
        }
        for(int i=1; i<2; i++){
            for(int j=0; j<FRAME_COLS; j++){
                walkUp[indexU++]=walkRegion[i][j];
            }
        }
        for(int i=2; i<3; i++){
            for(int j=0; j<FRAME_COLS; j++){
                walkLeft[indexL++]=walkRegion[i][j];
            }
        }
        for(int i=3; i<4; i++){
            for(int j=0; j<FRAME_COLS; j++){
                walkRight[indexR++]=walkRegion[i][j];
            }
        }
        //Instantiate animations at duration 0.15f
        downAnim=new Animation<TextureRegion>(0.15f, walkDown);
        upAnim=new Animation<TextureRegion>(0.15f, walkUp);
        leftAnim=new Animation<TextureRegion>(0.15f, walkLeft);
        rightAnim=new Animation<TextureRegion>(0.15f, walkRight);
        swordAnims=new Animation<TextureRegion>(0.08f, swordAnim);
        stateTime=0f;
        stateTime1=0f;
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        isTalking=false;
        this.game=game;
        //This is for loading maps. To see a full documentation on how maps are loaded check the "Wiki" tab on the Github.
        one=prefs.getInteger("one",1);
        two=prefs.getInteger("two", 0);
        //Don't worry about this
        camera=new OrthographicCamera();
        camera.setToOrtho(false, 832, 512);
        camera.update();


        //Instantiates batch, a group of sprites
        batch = new SpriteBatch();
        //Instantiates player and enemy images
        enemyImage = new Texture(Gdx.files.internal("bucket.png"));
        npcImage = new Texture(Gdx.files.internal("npc.png"));
        //Instantiate NPC hitbox
        if(one==1&&two==0)
            spawnNPC();
        //Sounds
        hit=Gdx.audio.newSound(Gdx.files.internal("woosh.mp3"));
        theme=Gdx.audio.newMusic(Gdx.files.internal("theme.mp3"));

        theme.setLooping(true);
        theme.play();

        //Instantiates hitboxes for player and enemies
        player = new Rectangle();
        player.x = prefs.getFloat("playerx", 832 / 2 - 64 / 2);
        player.y = prefs.getFloat("playery", 20);
        player.width = 48;
        player.height = 64;

        //Array of enemy hitboxes
        enemies = new Array<Rectangle>();
        //Go to spawn enemy method
        spawnEnemy();

        //For more information, see the Wiki on the Github
        loadMap(one, two);

        createText(bob.dialogue);

    }

    @Override
    public void render (float delta) {
        //Clears screen
        Gdx.gl.glClearColor(0.2f, 1, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Refreshes screen, I believe it's 60 fps
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        //Don't worry about this
        batch.setProjectionMatrix(camera.combined);
        //Begin drawing
        batch.begin();
        batch.draw(tree, 0, 0);
        //Draw NPC at NPC position
        if(one==1&&two==0) {
            for(Rectangle r:talkers)
                batch.draw(bob.image, r.x, r.y);
        }
        //Draw enemies at their respective positions
        for(Rectangle enemy: enemies) {
            batch.draw(enemyImage, enemy.x, enemy.y);
        }
        //Draws a textbox if isTalking is true
        if (isTalking) {
            tb.draw(batch, 1.0f);
        }
        //Player movement script. I use two booleans, talk and isTalking, to control player movement. Don't worry too much about
        //because I'm pretty sure this is bug-free now.
        if(talk)
            tryTalking();
        if(!isTalking){
            if(Gdx.input.isKeyPressed(Input.Keys.Z)){
                slash=true;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.LEFT)||Gdx.input.isKeyPressed(Input.Keys.A)){
                //Loops through next 200 pixels individually to check collisions to allow pixel perfect onesZ
                for(int x=0; x<200;x++) {
                    if(talkers.size>0) {
                        for (Rectangle r : talkers) {
                            if (new Rectangle(player.x - 1, player.y, 48f, 64f).overlaps(r)) {
                                talk = true;
                            } else {
                                player.x -= Gdx.graphics.getDeltaTime();
                                talk = false;
                                prefs.putFloat("playerx", player.x);
                            }
                        }
                    }
                    else{
                        player.x -= Gdx.graphics.getDeltaTime();
                        talk = false;
                        prefs.putFloat("playerx", player.x);
                    }
                }
                //Changes default image to face left if you stop moving
                stayStill=new Texture(Gdx.files.internal("staticLeft.png"));
                if(!(Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W)||Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D)||Gdx.input.isKeyPressed(Input.Keys.DOWN)||Gdx.input.isKeyPressed(Input.Keys.S))){
                    //Walk left
                    animateMove(leftAnim);
                }
            }
            if(Gdx.input.isKeyPressed(Input.Keys.DOWN)||Gdx.input.isKeyPressed(Input.Keys.S)){
                //Loops through next 200 pixels individually to check collisions to allow pixel perfect ones
                for(int x=0; x<200;x++) {
                    if(talkers.size>0) {
                        for (Rectangle r : talkers) {
                            if (new Rectangle(player.x, player.y - 1, 48f, 64f).overlaps(r)) {
                                talk = true;
                            } else {
                                player.y -= Gdx.graphics.getDeltaTime();
                                talk = false;
                                prefs.putFloat("playery", player.y);
                            }
                        }
                    }
                    else{
                        player.y -= Gdx.graphics.getDeltaTime();
                        talk = false;
                        prefs.putFloat("playery", player.y);
                    }
                }
                stayStill=new Texture(Gdx.files.internal("static.png"));
                if(!(Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W)||Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D))){
                    animateMove(downAnim);
                }
            }
            if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D)){
                //Loops through next 200 pixels individually to check collisions to allow pixel perfect ones
                for(int x=0; x<200;x++) {
                    if(talkers.size>0) {
                        for (Rectangle r : talkers) {
                            if (new Rectangle(player.x + 1, player.y, 48f, 64f).overlaps(r)) {
                                talk = true;
                            } else {
                                player.x += Gdx.graphics.getDeltaTime();
                                talk = false;
                                prefs.putFloat("playerx", player.x);
                            }
                        }
                    }
                    else{
                        player.x += Gdx.graphics.getDeltaTime();
                        talk = false;
                        prefs.putFloat("playerx", player.x);
                    }
                }
                stayStill=new Texture(Gdx.files.internal("staticRight.png"));
                if(!(Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W))){
                    animateMove(rightAnim);
                }
            }
            if(Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W)){
                //Loops through next 200 pixels individually to check collisions to allow pixel perfect ones
                for(int x=0; x<200;x++) {
                    if(talkers.size>0) {
                        for (Rectangle r : talkers) {
                            if (new Rectangle(player.x, player.y + 1, 48f, 64f).overlaps(r)) {
                                talk = true;
                            } else {
                                player.y += Gdx.graphics.getDeltaTime();
                                talk = false;
                                prefs.putFloat("playery", player.y);
                            }
                        }
                    }
                    else{
                        player.y += Gdx.graphics.getDeltaTime();
                        talk = false;
                        prefs.putFloat("playery", player.y);
                    }
                }
                stayStill=new Texture(Gdx.files.internal("staticUp.png"));
                animateMove(upAnim);
            }
            if(!(Gdx.input.isKeyPressed(Input.Keys.LEFT)||Gdx.input.isKeyPressed(Input.Keys.A)||Gdx.input.isKeyPressed(Input.Keys.UP)||Gdx.input.isKeyPressed(Input.Keys.W)||Gdx.input.isKeyPressed(Input.Keys.DOWN)||Gdx.input.isKeyPressed(Input.Keys.S)||Gdx.input.isKeyPressed(Input.Keys.RIGHT)||Gdx.input.isKeyPressed(Input.Keys.D)))
               //If no keys are being pressed, stand still.
                batch.draw(stayStill, player.x, player.y);
        }
        else
            //If being talked to, stand still
            batch.draw(stayStill, player.x, player.y);
        if(slash){
            stateTime1+= Gdx.graphics.getDeltaTime();
            TextureRegion currentFrame = swordAnims.getKeyFrame(stateTime1,true);
            System.out.println(swordAnims.getKeyFrameIndex(stateTime1));
            batch.draw(currentFrame, player.x+44, player.y);
            if(swordAnims.getKeyFrameIndex(stateTime1)==4) {
                stateTime1=0;
                slash = false;
            }
        }
        batch.end();

        //Make sure you don't go out of bounds and will teleport you to the next map.
        if(player.x < 0){
            one-=2;
            if(Gdx.files.internal("test"+one+""+two+".tmx").exists()) {
                player.x = 832-64;
                loadMap(one, two);
                talkers.clear();
                if(one==1&&two==0)
                    spawnNPC();
            }
            else{
                one+=2;
                checkBounds(player);
            }
            prefs.putInteger("one", one);
            prefs.putInteger("two", two);
        }
        if(player.x > 832 - 64){
            one+=2;
            if(Gdx.files.internal("test"+one+""+two+".tmx").exists()) {
                player.x = 0;
                loadMap(one, two);
                talkers.clear();
                if(one==1&&two==0)
                    spawnNPC();
            }
            else{
                one-=2;
                checkBounds(player);
            }
            prefs.putInteger("one", one);
            prefs.putInteger("two", two);
        }
        if(player.y < 0){
            one--;
            two--;
            if(Gdx.files.internal("test"+one+""+two+".tmx").exists()) {
                player.y = 512-64;
                loadMap(one, two);
                talkers.clear();
                if(one==1&&two==0)
                    spawnNPC();
            }
            else{
                one++;
                two++;
                checkBounds(player);
            }
            prefs.putInteger("one", one);
            prefs.putInteger("two", two);
        }
        if(player.y > 512 - 64){
            one++;
            two++;
            if(Gdx.files.internal("test"+one+""+two+".tmx").exists()) {
                player.y = 0;
                loadMap(one, two);
                talkers.clear();
                if(one==1&&two==0)
                    spawnNPC();
            }
            else{
                one--;
                two--;
                checkBounds(player);
            }
            prefs.putInteger("one", one);
            prefs.putInteger("two", two);
        }
        //Moves enemies. It loops through each enemy hitbox (a rectangle) and there is a 1/300 chance it will move a certain direction.
        //Since this is probably 60 fps this results in the enemy moving in a random direction approximately once every 5 seconds.
        Iterator<Rectangle> iter = enemies.iterator();
        while(iter.hasNext()){
            //Move to next rectangle
            Rectangle enemy = iter.next();
            if(MathUtils.random(0,350)==1)
                enemy.y-=2000*Gdx.graphics.getDeltaTime();
            else if(MathUtils.random(0,350)==2)
                enemy.x-=2000*Gdx.graphics.getDeltaTime();
            else if(MathUtils.random(0,350)==3)
                enemy.x+=2000*Gdx.graphics.getDeltaTime();
            else if(MathUtils.random(0,350)==4)
                enemy.y+=2000*Gdx.graphics.getDeltaTime();
            if(enemy.overlaps(player)){
                game.setScreen(new BattleScreen(game, enemyImage, walkSheet));
                prefs.flush();
                iter.remove();
                dispose();
            }

            //Make sure enemy doesn't leave screen
            checkBounds(enemy);

            //Saves your location every five seconds. Not optimal at all.
            push++;
            if(push%300==0)
                prefs.flush();
        }
    }

    private void animateMove(Animation<TextureRegion> animate) {
        stateTime+= Gdx.graphics.getDeltaTime();

        TextureRegion currentFrame = animate.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, player.x, player.y);
    }

    private void checkBounds(Rectangle object) {
        if(object.x < 0) object.x = 0;
        if(object.x > 832 - 64) object.x = 832 - 64;
        if(object.y < 0) object.y = 0;
        if(object.y > 512 - 64) object.y = 512 - 64;
    }
    public void spawnNPC(){
        Rectangle npcBox;
        npcBox=new Rectangle();
        npcBox.x = 832 / 2 - 64 / 2;
        npcBox.y = 200;
        npcBox.width = 64;
        npcBox.height = 64;
        talkers.add(npcBox);
    }
    private void spawnEnemy() {
        //Spawns 3 random enemies
        for(int x=0; x<3; x++) {
            Rectangle enemy = new Rectangle();
            //Spawn at random x and y coords
            enemy.x = MathUtils.random(100, 732 - 64);
            enemy.y = MathUtils.random(50, 482 - 64);
            enemy.width = 64;
            enemy.height = 64;
            enemies.add(enemy);
        }
    }
    public void tryTalking(){
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            // Use a helper so that a held-down button does not continuously switch between states with every tick
            if (talkHelper) {
                System.out.print(isTalking+", ");
                if (isTalking) {
                    isTalking=false;
                }
                else {
                    isTalking = true;
                }
                System.out.println(isTalking);
                talkHelper = false;
            }
        }
        else
            talkHelper=true;
    }

    private void loadMap(int mapNo1, int mapNo2){
        TiledMap newMap;
        newMap = new TmxMapLoader().load("test"+mapNo1+""+mapNo2+".tmx");
        TiledMap map = newMap;
            tiledMapRenderer = new OrthogonalTiledMapRenderer(map);
            enemies.clear();
            spawnEnemy();
    }
    //Don't worry about everything below this
    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose () {
        batch.dispose();
        walkSheet.dispose();
        enemyImage.dispose();
        theme.dispose();
    }
    private void createText(String text)
    {
        //Don't worry about this. All you need to know is if you want to create a textbox use createText("Hello World");
        Skin skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("default", new BitmapFont());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0, 0, 0, 0.7f));
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        tb = new TextButton(text, skin);
        tb.setX(0);
        tb.setY(0);
        tb.setWidth(Gdx.graphics.getWidth());
        tb.setHeight(120);
        tb.setVisible(true);
    }
}
