package io.github.nickelme.Physics_Sandbox;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

public class PhysicsWorld {
	
	private btDiscreteDynamicsWorld dynworld;
	private btSequentialImpulseConstraintSolver solver;
	private float stepSpeed = 1.0f;
	private btBroadphaseInterface broadphase;
	private btDefaultCollisionConfiguration collisionConfiguration;
	private btCollisionDispatcher dispatcher;
	
	public PhysicsWorld(){
		broadphase = new btDbvtBroadphase();
		collisionConfiguration = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfiguration);
		solver = new btSequentialImpulseConstraintSolver();
		dynworld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		dynworld.setGravity(new Vector3(0,-10,0));
	}
	
	
	public void AddObject(PSObject object){
		dynworld.addRigidBody(object.getRigidBody());
	}
	
	public void Stimulate(float deltatime){
		dynworld.stepSimulation(deltatime * stepSpeed, 8);
	}
	
	public float getStepSpeed(){
		return stepSpeed;
	}
	
	public void setStepSpeed(float newSpeed){
		stepSpeed = newSpeed;
	}
	
	public void increaseStepSpeed(){
		setStepSpeed(getStepSpeed() + 0.25f);
	}
	
	public void decreaseStepSpeed(){
		setStepSpeed(getStepSpeed() - 0.25f);
	}
	
	public void resetStepSpeed(){
		setStepSpeed(1.0f);
	}
	
	public void ClearObject(PSObject object){
		dynworld.removeRigidBody(object.getRigidBody());
	}
	
	public btDiscreteDynamicsWorld getWorld(){
		return dynworld;
	}
}
