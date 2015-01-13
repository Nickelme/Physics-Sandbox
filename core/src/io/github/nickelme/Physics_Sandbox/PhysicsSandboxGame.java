package io.github.nickelme.Physics_Sandbox;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import sun.security.krb5.internal.NetClient;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController.Transform;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Interpolation.Exp;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class PhysicsSandboxGame extends ApplicationAdapter {

	private FirstPersonCameraController fpcontrol;
	public PerspectiveCamera cam;
	private ModelBatch modelBatch;
	public List<PSObject> Objects = new ArrayList<PSObject>();
	private PhysicsWorld world;
	private Long lasttick;
	private Environment env;
	private PhysicUserInput physin;
	private InputMultiplexer inplex;
	private AssetManager assetman;
	private Overlay overlay;
	public Controller controllerClass;
	private DebugDrawer dDrawer;
	
	private boolean iscoin = false;
	
	private static PhysicsSandboxGame instance;
	
	public boolean bDebugRender = false;
	
	private LeapController handController;
	
	@Override
	public void create () {
		instance = this;
		overlay = new Overlay(this);
		controllerClass = new Controller(this);
		
		handController = new LeapController();
		
		assetman = new AssetManager();
		Bullet.init();
		modelBatch = new ModelBatch();
		
		
		cam = new PerspectiveCamera(90, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(100f, 0f, 100f);
		cam.lookAt(0,0,0);
		cam.near = 2f;
		cam.far = 1000000f;
		cam.update(true);



		world = new PhysicsWorld();
		//world.getWorld().setGravity(new Vector3());
		Floor floor = new Floor(new Vector3(100000,1,100000),  new Matrix4(new Vector3(0,-5,0), new Quaternion(), new Vector3(1,1,1)));
		floor.SetColor(Color.DARK_GRAY);
		Objects.add(floor);
		world.AddObject(floor);
		CreateCubeOfCubes();
		
		inplex = new InputMultiplexer();
		physin = new PhysicUserInput(this);
		

		fpcontrol = new FirstPersonCameraController(cam);
		fpcontrol.setVelocity(50.0f);

		Gdx.input.setInputProcessor(inplex);
		

		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, new Color(0.5f,0.5f,0.5f, 1.0f)));
		env.add(new DirectionalLight().set(Color.WHITE, new Vector3(0,-90, 0)));
		
		lasttick = System.currentTimeMillis();

	    inplex.addProcessor(overlay.getStage());
	    inplex.addProcessor(physin);
	    inplex.addProcessor(fpcontrol);
	    
	    
	    dDrawer = new DebugDrawer();
	    dDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

	    world.getWorld().setDebugDrawer(dDrawer);
	   
	    
	}        

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportWidth = height;
		cam.update(true);
		overlay.ScreenResized(width, height);
		
	};

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		world.Stimulate(Gdx.graphics.getDeltaTime());
		for(int i = 0; i<Objects.size(); i++){
			Objects.get(i).Update();
		}
		fpcontrol.update();
		cam.update();
		if (iscoin){
			Vector3 vec = new Vector3();
			for(int i = 0; i<Objects.size(); i++){
				if((Objects.get(i) instanceof ModelObject)){
					ModelObject obj = (ModelObject) Objects.get(i);
					if(obj.getPath().equalsIgnoreCase("Quater/Quater.obj")){
						vec = obj.getRigidBody().getWorldTransform().getTranslation(vec);
						System.out.println(vec);
					}
				}
			}
			cam.up.x = 0.0f;
			cam.up.z = 0.0f;
			cam.lookAt(vec);
		}

		modelBatch.begin(cam);
		for(int i = 0; i <Objects.size(); i++){
			modelBatch.render(Objects.get(i), env);
		}
		
		modelBatch.end();
		if(bDebugRender){
			dDrawer.begin(cam);
	    	world.getWorld().debugDrawWorld();
	    	dDrawer.end();
		}
		lasttick = System.currentTimeMillis();
		
		overlay.Draw();
		controllerClass.Nudge();
		
		//Explode(new Vector3(10, 10, 10), 1000, 1000);
		
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		overlay.dispose();

	}

	public Camera getCamera(){
		return cam;
	}

	public void addObject(PSObject obj){
		Objects.add(obj);
		world.AddObject(obj);
	}
	
	public AssetManager getAssetManager(){
		return assetman;
	}
	
	public void updateSliders(){
		//rValue.
	}
	
	public PhysicsWorld getPhysicsWorld(){
		return world;
	}
	
	public PhysicUserInput getPhysicsInput(){
		return physin;
	}
	
	public void ClearWorld(){
		iscoin = false;
		for(int i = 0; i<Objects.size(); i++){
			if(!(Objects.get(i) instanceof Floor)){
				world.ClearObject(Objects.get(i));
				Objects.remove(i);
				i--;
			}
		}
	}

	public void CreateCubeOfCubes() {
		for(int x = 0; x < 8; x++){
			for(int y=0; y < 8; y++){
				for(int z=0; z< 8; z++){
					PrimitiveCube cube = new PrimitiveCube(new Vector3(5,5,5), new Matrix4(new Vector3(x*10,(y*10),z*10), new Quaternion(), new Vector3(1,1,1)));
					Objects.add(cube);
					world.AddObject(cube);
					
				}
			}
		}
	}
	
	public void CreateCubePyramid(){
		for(int y = 6; y > 0; y--){
			for(int z = -y; z < y; z++){
				for(int x = -y; x < y; x++){
					PrimitiveCube cube = new PrimitiveCube(new Vector3(5,5,5), new Matrix4(new Vector3(x*10,(5-y)*10,z*10), new Quaternion(), new Vector3(1,1,1)));
					Objects.add(cube);
					world.AddObject(cube);
				}
			}
		}
	}
	
	
	public void Explode(Vector3 loc, float size, float force){
		for(int i = 0; i<Objects.size(); i++){
			PSObject obj = Objects.get(i);
			Matrix4 objtrans = obj.getRigidBody().getWorldTransform();
			Vector3 objloc = new Vector3(0,0,0);
			objtrans.getTranslation(objloc);
			if(loc.dst(objloc) < size){
				Vector3 workvec = new Vector3(objloc);
				workvec.sub(loc);
				workvec.nor();
				workvec.scl(force);
				
				//System.out.println("\tAngle: " + Math.toDegrees((Math.atan2(loc.y-objloc.y, loc2d.dst(objloc2d)))));
				if(!obj.getRigidBody().isActive()){
					obj.getRigidBody().activate();
				}
				obj.getRigidBody().applyCentralForce(workvec);
			}
		}
	}
	
	public void FlipCoin(){
		for(int i = 0; i<Objects.size(); i++){
			if((Objects.get(i) instanceof ModelObject)){
				ModelObject obj = (ModelObject) Objects.get(i);
				if(obj.getPath().equalsIgnoreCase("Quater/Quater.obj")){
					obj.getRigidBody().activate();
					float flipforce = (float) (Math.random()*500+300);
					System.out.println("Flipping with force: " + flipforce);
					obj.getRigidBody().applyImpulse(new Vector3(0, flipforce, 0), new Vector3(5,0,0));
				}
			}
		}
	}
	
	public void RemoveObject(PSObject obj){
		world.ClearObject(obj);
		Objects.remove(Objects.indexOf(obj));
	}
	
	public static PhysicsSandboxGame getInstance(){
		return instance;
	}
	
	public PSObject getObjectAt(int i){
		return Objects.get(i);
	}
	
	public int getNumberOfObjects(){
		return Objects.size();
	}
	
	public void CreateCoinFlip(){
		ModelObject obj = new ModelObject("Quater/Quater.obj", new Matrix4(new Vector3(0,30,0), new Quaternion(), new Vector3(1.0f, 1.0f, 1.0f)), false);
		Objects.add(obj);
		world.AddObject(obj);
		iscoin = true;
	}
	
	public void CreateBowlingAlley(){
		for(int i = 1; i<10; i++){
			for(int j = 0; j<i; j++){
				ModelObject obj = new ModelObject("BowlingPin/Bowling Pin.obj", new Matrix4(new Vector3(i*30.0f, 0.0f, (j*30.0f)-((i*30.0f)/2)), new Quaternion(), new Vector3(1.0f, 1.0f, 1.0f)), false);
				Objects.add(obj);
				world.AddObject(obj);
			}
		}
	}
	
	public void CustomCubeOfCubes(int a, int b, int c){
		for(int x = 0; x < a; x++){
			for(int y=0; y < b; y++){
				for(int z=0; z< c; z++){
					PrimitiveCube cube = new PrimitiveCube(new Vector3(5,5,5), new Matrix4(new Vector3(x*10,(y*10),z*10), new Quaternion(), new Vector3(1,1,1)));
					Objects.add(cube);
					world.AddObject(cube);
				}
			}
		}
	}
	
	public void CustomPyramidOfCubes(int a){
		for(int y = a; y > 0; y--){
			for(int z = -y; z < y; z++){
				for(int x = -y; x < y; x++){
					PrimitiveCube cube = new PrimitiveCube(new Vector3(5,5,5), new Matrix4(new Vector3(x*10,((a-1)-y)*10,z*10), new Quaternion(), new Vector3(1,1,1)));
					Objects.add(cube);
					world.AddObject(cube);
				}
			}
		}
	}
	
	public void resetCamera(){
		cam.position.set(100f, 0f, 100f);
		cam.lookAt(0,0,0);
		cam.near = 2f;
		cam.far = 1000000f;
		cam.up.x = 0.0f;
		cam.up.z = 0.0f;
		cam.update(true);
	}
}



class Floor extends PrimitiveCube{

	public Floor(Vector3 size, Matrix4 transform) {
		super(size, transform);
	}

	@Override
	public btRigidBody getRigidBody() {
		if(rigidbody == null){
			btCollisionShape groundShape = new btStaticPlaneShape(new Vector3(0, 1, 0), 1);
			btDefaultMotionState fallMotionState = new btDefaultMotionState(worldTransform);
	        Vector3 fallInertia = new Vector3(0, 0, 0);
	        groundShape.calculateLocalInertia(100000, fallInertia);
			btRigidBodyConstructionInfo fallRigidBodyCI = new btRigidBodyConstructionInfo(0, fallMotionState, groundShape, fallInertia);
			rigidbody = new btRigidBody(fallRigidBodyCI);
		}
		return rigidbody;
	}

}