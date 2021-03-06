package com.pzmatty.rolesandbox.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pzmatty.rolesandbox.controllers.CursorController;
import com.pzmatty.rolesandbox.objects.GameObject;
import com.pzmatty.rolesandbox.objects.ISwitch;
import com.pzmatty.rolesandbox.objects.entities.AnimatedEntity;
import com.pzmatty.rolesandbox.objects.entities.Character;
import com.pzmatty.rolesandbox.objects.entities.Entity;
import com.pzmatty.rolesandbox.objects.entities.Monster;
import com.pzmatty.rolesandbox.objects.entities.StaticEntity;
import com.pzmatty.rolesandbox.objects.entities.Trigger;
import com.pzmatty.rolesandbox.objects.entities.switchs.DoorSwitch;

public class TiledMapManager {

	public static enum ActionState {
		PLAYER, CURSOR
	}

	private static final String TAG = TiledMapManager.class.getSimpleName();

	public static final float WORLD_TO_SCREEN = 1 / 16f;
	public static final float WORLD_UNIT = 16;
	public static final float WORLD_WIDTH = 26;
	public static final float WORLD_HEIGHT = 16;
	public static final float ASPECT_RATIO = Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
	private ActionState state;

	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Viewport viewport;

	private Array<Monster> monsters;
	private Array<Character> characters;
	private Array<ISwitch> switchs;
	private Array<Trigger> triggers;
	private Array<Entity> props;
	private Array<StaticEntity> items;
	private Array<StaticEntity> tiles;

	private TiledMapTileLayer tileLayer;
	private TiledMapTileLayer propLayer;

	private MapObjects objectLayer;

	private Monster player;

	private String mapName;

	public TiledMapManager(SpriteBatch batch, String mapName) {
		this.mapName = mapName;
		this.state = ActionState.PLAYER;
		this.monsters = new Array<>();
		this.characters = new Array<>();
		this.switchs = new Array<>();
		this.triggers = new Array<>();
		this.props = new Array<>();
		this.items = new Array<>();
		this.tiles = new Array<>();

		this.camera = new OrthographicCamera();
		//this.viewport = new FitViewport(WORLD_WIDTH * ASPECT_RATIO, WORLD_HEIGHT, camera);
		this.viewport = new ExtendViewport(WORLD_WIDTH * ASPECT_RATIO, WORLD_HEIGHT, camera);

		this.batch = batch;

		loadMap(mapName);
	}

	public void addMonster(Monster entity) {
		monsters.add(entity);
	}

	public boolean checkEntities(Entity entity, Vector2 position) {
		for (Monster other : monsters) {
			if (other.getPosition().equals(position) && other.isBlock() && other != entity) {
				Gdx.app.log(TAG, "Character collide");
				return true;
			}
		}
		for (ISwitch other : switchs) {
			if (((GameObject) other).getPosition().equals(position) && ((GameObject) other).isBlock()
					&& other != entity) {
				other.toogle();
				Gdx.app.log(TAG, "Switch collide");
				return true;
			}
		}
		for (Entity prop : props) {
			if (prop.getPosition().equals(position) && prop.isBlock()) {
				Gdx.app.log(TAG, "Prop collide");
				return true;
			}
		}
		for (StaticEntity item : items) {
			if (item.getPosition().equals(position) && item.isBlock()) {
				Gdx.app.log(TAG, "Item collide");
				return true;
			}
		}
		return false;
	}

	public boolean checkObjects(Entity entity, Vector2 position) {
		Rectangle r = entity.getRect();
		r.setPosition(position.x, position.y);
		for (MapObject object : objectLayer) {
			if (object instanceof RectangleMapObject) {
				Rectangle rect = ((RectangleMapObject) object).getRectangle();
				if (r.overlaps(rect) && object.getProperties().get("collides", Boolean.class) == true) {
					Gdx.app.log(TAG, "Object collide");
					return true;
				}
			}
		}
		return false;
	}

	public boolean collides(Entity entity, Vector2 position) {
		for (StaticEntity tile : tiles) {
			if (tile.getPosition().equals(position) && tile.isBlock()) {
				Gdx.app.log(TAG, "Block collide");
				return true;
			}
		}
		return false;
	}

//	public void createCharacter(Rectangle rectangle, MapProperties properties) {
//		characters.add(new Character(AssetsManager.getAnimated(properties.get("name", String.class), "CHAR"),
//				rectToWorld(rectangle), true, properties.get("name", String.class)));
//	}

//	public void createCharacter(String name, Rectangle rectangle) {
//		Array<String> properties = DatabaseManager.getCharacterStats(name);
//		Character chr = new Character(AssetsManager.getAnimated(name, "MON"), rectToWorld(rectangle), true, name);
//
//		chr.setProperties(properties.get(0), properties.get(1), properties.get(2), properties.get(3),
//				Integer.parseInt(properties.get(4)), Integer.parseInt(properties.get(5)),
//				Integer.parseInt(properties.get(6)), Integer.parseInt(properties.get(7)),
//				Integer.parseInt(properties.get(8)), Integer.parseInt(properties.get(9)));
//		characters.add(chr);
//
//	}
	
//	public void createMonster(Rectangle rectangle, MapProperties properties) {
//		monsters.add(new Monster(AssetsManager.getAnimated(properties.get("name", String.class), "CHAR"),
//				rectToWorld(rectangle), true, properties.get("name", String.class)));
//	}

	public void createMonster(String name, Rectangle rectangle) {
		Monster mon = new Monster(AssetsManager.getAnimated(name, "MON"), rectToWorld(rectangle), true, name, DatabaseManager.getMonsterStats(name));
		monsters.add(mon);

	}

	public void createDoor(Rectangle rectangle, MapProperties properties) {
		String align = properties.get("align", String.class);
		switchs.add(new DoorSwitch(AssetsManager.getAnimated(properties.get("name", String.class), "SWITCH", align),
				rectToWorld(rectangle), true, properties.get("name", String.class)));
	}

	public void dispose() {
		map.dispose();
		renderer.dispose();
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public Character getCharacter(int index) {
		if (index <= characters.size)
			return characters.get(index);
		else
			return characters.get(0);
	}
	
	public Monster getMonster(int index) {
		if (index <= monsters.size)
			return monsters.get(index);
		else {
			return monsters.get(0);
		}
	}

	public TiledMap getMap() {
		return map;
	}

	public Entity getPlayer() {
		return player;
	}

	public Vector2 getSpawnPosition(String tag) {
		Vector2 position = new Vector2();
		if (objectLayer != null && objectLayer.get(tag) != null) {
			position.x = (float) objectLayer.get(tag).getProperties().get("x");
			position.y = (float) objectLayer.get(tag).getProperties().get("y");
		}
		return position;
	}

	public Array<String> getTileInfo(Vector2 position) {
		Array<String> info = new Array<>();
		for (StaticEntity item : items) {
			if (item.getPosition().equals(position)) {
				info.add(item.getName());
				break;
			}
		}
		for (Entity prop : props) {
			if (prop.getPosition().equals(position)) {
				info.add(prop.getName());
				break;
			}
		}
		for (ISwitch other : switchs) {
			if (((GameObject) other).getPosition().equals(position)) {
				info.add(((Entity) other).getName());
				break;
			}
		}
		for (StaticEntity tile : tiles) {
			if (tile.getPosition().equals(position)) {
				info.add(tile.getName());
				break;
			}
		}
		return info;
	}
	
	public Array<String> getMonsterInfo(Vector2 position) {
		for (Monster other : monsters) {
			if (other.getPosition().equals(position)) {
				return other.getProperties();
			}
		}
		return null;
	}

	public Viewport getViewport() {
		return viewport;
	}

	public void loadMap(String mapName) {
		this.mapName = mapName;
		this.map = AssetsManager.get(DatabaseManager.getConstant(mapName), TiledMap.class);

		this.renderer = new OrthogonalTiledMapRenderer(map, WORLD_TO_SCREEN, batch);

		this.map.getLayers().get("MarkLayer").setVisible(false);
		this.tileLayer = (TiledMapTileLayer) map.getLayers().get("TileLayer");

		this.propLayer = (TiledMapTileLayer) map.getLayers().get("PropLayer");
		this.objectLayer = map.getLayers().get("ObjectLayer").getObjects();

		Vector2 playerPos = getSpawnPosition("Spawn.PlayerSpawn");
		Rectangle playerRect = new Rectangle(playerPos.x, playerPos.y, 16, 16);
		createMonster("Paesant", playerRect);
		this.player = monsters.get(0);
		setCameraPosition(player.getPosition());
		loadMapObjects();
	}

	private void loadMapObjects() {
		// Load map tiles
		for (int x = 0; x < tileLayer.getWidth(); x++) {
			for (int y = 0; y < tileLayer.getHeight(); y++) {
				if (tileLayer.getCell(x, y) != null) {
					TextureRegion region = tileLayer.getCell(x, y).getTile().getTextureRegion();
					MapProperties properties = tileLayer.getCell(x, y).getTile().getProperties();
					tiles.add(new StaticEntity(region, new Rectangle(x, y, 1, 1), properties.containsKey("collides"),
							properties.get("name", String.class)));
				}
			}
		}

		// Load map props
		for (int x = 0; x < propLayer.getWidth(); x++) {
			for (int y = 0; y < propLayer.getHeight(); y++) {
				if (tileLayer.getCell(x, y) != null) {
					try {
						if (propLayer.getCell(x, y).getTile() != null) {
							TiledMapTile tile = propLayer.getCell(x, y).getTile();
							MapProperties properties = tile.getProperties();
							if (tile instanceof StaticTiledMapTile) {
								TextureRegion region = tile.getTextureRegion();
								props.add(new StaticEntity(region, new Rectangle(x, y, 1, 1),
										properties.containsKey("collides"), properties.get("name", String.class)));
							} else if (tile instanceof AnimatedTiledMapTile) {
								StaticTiledMapTile[] tiles = ((AnimatedTiledMapTile) tile).getFrameTiles();
								TextureRegion[] regions = { tiles[0].getTextureRegion(), tiles[1].getTextureRegion() };
								props.add(new AnimatedEntity(regions, new Rectangle(x, y, 1, 1),
										properties.containsKey("collides"), true,
										properties.get("name", String.class)));
							}
						}
					} catch (Exception ex) {
					}
				}
			}
		}

		// Load map objects
		for (MapObject object : objectLayer) {
			String[] parts = object.getName().split("[.]");
			RectangleMapObject rectangleObject = (RectangleMapObject) object;
			Rectangle rectangle = rectangleObject.getRectangle();
			MapProperties properties = object.getProperties();
			// Load switch objects
			if (parts[0].equals("Switch")) {
				if (parts[1].equals("Door")) {
					createDoor(rectangle, properties);
				}
				// Load monsters
			} else if (parts[0].equals("Mon")) {
				createMonster(parts[1], rectangle);
			} else if (parts[0].equals("Trigger")) {
				triggers.add(new Trigger(rectToWorld(rectangle), parts[1]));
			}
		}
	}

	private Rectangle rectToWorld(Rectangle rectangle) {
		return new Rectangle(rectangle.x * TiledMapManager.WORLD_TO_SCREEN,
				rectangle.y * TiledMapManager.WORLD_TO_SCREEN, rectangle.width * TiledMapManager.WORLD_TO_SCREEN,
				rectangle.height * TiledMapManager.WORLD_TO_SCREEN);
	}

	public void render(float delta) {

		camera.update();
		//camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
		renderer.setView(camera);
		renderer.render();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (StaticEntity tile : tiles) {
			tile.draw(batch);
		}
		for (Entity prop : props) {
			prop.draw(batch);
		}
		for (StaticEntity item : items) {
			item.draw(batch);
		}
		for (ISwitch sw : switchs) {
			((Entity) sw).draw(batch);
		}
		if (state.equals(ActionState.CURSOR)) {
			CursorController.getCursor().draw(batch);
		}
		for (Monster monster : monsters) {
			monster.draw(batch);
		}
		batch.end();
	}

	public void resize(int width, int height) {
		viewport.update((int) (width * ASPECT_RATIO), height);
		camera.update();
	}

	public void setCameraPosition(Vector2 vector) {
		camera.position.set(vector.x + (TiledMapManager.WORLD_UNIT / 2) * TiledMapManager.WORLD_TO_SCREEN,
				vector.y + (TiledMapManager.WORLD_UNIT / 2) * TiledMapManager.WORLD_TO_SCREEN, 0.0f);
	}

	public void setPlayer(Monster monster) {
		player = monster;
	}

	public void setState(ActionState state) {
		this.state = state;
	}

	public void translateCamera(Vector2 vector) {
		camera.translate(vector);
	}

	public void triggerOnEnter(Vector2 position) {
		for (Trigger trigger : triggers) {
			/*
			 * Gdx.app.log(TAG, "x: " + trigger.getPosition().x + ", y: " +
			 * trigger.getPosition().y + ", width: " + trigger.getRect().getWidth() +
			 * ", height: " + trigger.getRect().getHeight()); Gdx.app.log(TAG,
			 * "Player - x: " + position.x + ", y: " + position.y); Gdx.app.log(TAG,
			 * String.valueOf(trigger.getRect().contains(position)));
			 */
			if (trigger.getEvent().equals("OnEnter") && trigger.getRect().contains(position)) {
				trigger.act();
				break;
			}
		}
	}

	public void triggerOnExit(Vector2 position, Vector2 lastRect) {
		for (Trigger trigger : triggers) {
			if (trigger.getEvent().equals("OnExit") && !trigger.getRect().contains(position)
					&& trigger.getRect().contains(lastRect)) {
				trigger.act();
				break;
			}
		}
	}

	public void unloadMap() {
		this.characters.clear();
		this.monsters.clear();
		this.tiles.clear();
		this.props.clear();
		this.items.clear();
		this.triggers.clear();
		this.switchs.clear();
		this.dispose();
	}

}
