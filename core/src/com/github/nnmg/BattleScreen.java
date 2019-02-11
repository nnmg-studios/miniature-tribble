package com.github.nnmg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

public class BattleScreen implements Screen {
    final Main game;
    private Music fight;
    private Texture enemyImg;
    private Texture playerImg;
    private OrthographicCamera camera;
    public BattleScreen(final Main game, Texture enemyImg, Texture playerImg){
        this.game=game;
        fight=Gdx.audio.newMusic(Gdx.files.internal("battle.wav"));
        fight.setLooping(true);
        fight.play();
        this.enemyImg=enemyImg;
        this.playerImg=playerImg;
        this.enemyImg = new Texture(Gdx.files.internal("bucket.png"));
        this.playerImg = new Texture(Gdx.files.internal("staticRight.png"));
        camera=new OrthographicCamera();
        camera.setToOrtho(false, 832, 512);
        camera.update();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.begin();
        game.font.draw(game.batch, "This is a ", 305, 340);
        game.font.draw(game.batch, "Battle Screen.", 305, 240);
        game.batch.draw(playerImg, 50, 225);
        game.batch.draw(enemyImg, 832-64-50, 225);
        game.batch.end();
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        System.out.println("pause");
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        fight.dispose();
        playerImg.dispose();
        enemyImg.dispose();
    }
}
