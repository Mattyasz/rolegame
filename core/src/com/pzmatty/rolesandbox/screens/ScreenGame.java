package com.pzmatty.rolesandbox.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.pzmatty.rolesandbox.RoleSandbox;
import com.pzmatty.rolesandbox.controllers.CursorController;
import com.pzmatty.rolesandbox.controllers.PlayerController;
import com.pzmatty.rolesandbox.managers.AssetsManager;
import com.pzmatty.rolesandbox.managers.DatabaseManager;
import com.pzmatty.rolesandbox.managers.SoundManager;
import com.pzmatty.rolesandbox.managers.TiledMapManager;
import com.pzmatty.rolesandbox.managers.UIManager;
import com.pzmatty.rolesandbox.ui.InfoGroupUI;

public class ScreenGame extends ScreenAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = ScreenBase.class.getSimpleName();
	private TiledMapManager map;
	private UIManager ui;
	private PlayerController controller;
	private CursorController cursor;
	private RoleSandbox game;

	public ScreenGame(RoleSandbox game) {
		this.game = game;
		map = new TiledMapManager(game.getBatch(), "MAP_01");
		ui = configUI();
		controller = new PlayerController(this, map.getPlayer());
		cursor = new CursorController(this);
		Gdx.input.setInputProcessor(controller);
	}

	@Override
	public void show() {
		ui.configActors();
		SoundManager.playMusic("MUSIC");
	}

	@Override
	public void render(float delta) {
		Gdx.gl20.glClearColor(.0f, .0f, .0f, 1.0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		map.render(delta);
		ui.render(delta);
	}

	@Override
	public void resize(int width, int height) {
		map.resize(width, height);
		// ui.resize(width, height);
	}

	@Override
	public void dispose() {
		map.dispose();
		ui.dispose();
	}

	private UIManager configUI() {
		return new UIManager(game.getBatch(), false) {

			@Override
			public void configActors() {
				Skin skin = AssetsManager.get(DatabaseManager.getConstant("SKIN_PATH"), Skin.class);

				addActor(new InfoGroupUI(skin), "Info");
				
				table.setFillParent(true);
				
				table.setWidth(Gdx.graphics.getWidth());
				table.top();
				
				table.add(getActor("Info", InfoGroupUI.class)).right().width(200).height(80).expandX();
				stage.addActor(table);
				
//				addActor(new Label("", skin, "default"), "InfoCursor");
//				 List<String> varList = new List<>(skin);
//
//				 ScrollPane scrollOptions = new ScrollPane(options, skin);
//				 ScrollPane scrollVariables = new ScrollPane(varList, skin);
//
//				table.setFillParent(true);
//
//				table.setWidth(Gdx.graphics.getWidth());
//				table.top();
//
//				getActor("InfoCursor", Label.class).setWrap(true);
//				options.align(Align.topLeft).pad(5);
//				options.columnLeft();
//
//				table.add();
//				table.add(getActor("InfoCursor", Label.class)).expandX().left().fillX().top().pad(10);
//				table.add(scrollVariables).right().width(120).fillY().top().pad(10).expandY().height(250);
//				table.row().height(100);
//				table.add(scrollOptions).bottom().fillX().fillY().pad(10).colspan(2);
//
//				stage.addActor(table);
			}

		};
	}

	public TiledMapManager getTiledMap() {
		return map;
	}

	public UIManager getUI() {
		return ui;
	}

	public PlayerController getPlayerController() {
		return controller;
	}

	public CursorController getCursorController() {
		return cursor;
	}

}
