package GarmBox;
import robocode.*;
import java.awt.Color;
import java.lang.Math;
import robocode.HitByBulletEvent;
import java.awt.geom.*;
import java.awt.Graphics2D;
import static robocode.util.Utils.*;
public class Anger extends TeamRobot
{
	String target;
	double swivel;		
	int count = 0;
	static String revenge = null;	
	static String lastBullet = null;
	double bulletPower = 3;
	double lastHeading = 0;

	public void run() {
	
		setColors(Color.red,Color.red,Color.red); // body,gun,radar
		setBulletColor(Color.red);
		setScanColor(new Color(51, 153, 255));
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
	
		swivel = 50;
		target = revenge;
		setTurnRadarRight(360);
		execute();
		while(true) {
			count++;
			scan();
			setTurnRadarRight(360);
			execute();
		}
	}


	public void onScannedRobot(ScannedRobotEvent e) {

		if (target != null && !e.getName().equals(target)){
			return;
		}
		
		if (isTeammate(e.getName())){
			return;
		}
		count = 0;
		
		if (target == null){ 
			target = e.getName();
			out.println("Tracking " + target);
		}
		
		double myX = getX();
		double myY = getY();
		double absBearing = Math.toRadians(getHeading() + e.getBearing());
		double enemyHeading = e.getHeadingRadians();
		double enemyHeadingChange = enemyHeading - lastHeading;
		double enemyX = myX + e.getDistance()*Math.sin(absBearing);
		double enemyY = myY + e.getDistance()*Math.cos(absBearing);
		double enemyVelocity = e.getVelocity();
		int ticks = 0;
		lastHeading = enemyHeading;
		out.println(enemyHeadingChange);
		
		double battleFieldHeight = getBattleFieldHeight();
		double battleFieldWidth = getBattleFieldWidth();
		double predictedX = enemyX;
		double predictedY = enemyY;
		while ((++ticks) * (20 - 3 * bulletPower) < Point2D.Double.distance(myX, myY, predictedX, predictedY)){
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			enemyHeading += enemyHeadingChange;
			
			if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
				break;
			}
		}
		Graphics2D g = getGraphics();
		g.setColor(Color.red);
        g.fillOval((int) predictedX, (int) predictedY, 20, 20);
		double theta = normalAbsoluteAngle(Math.atan2(predictedX - myX, predictedY - myY));

		swivel = normalRelativeAngle(absBearing - getRadarHeadingRadians());
		setTurnRadarRightRadians(swivel);
		swivel = normalRelativeAngle(theta - getGunHeadingRadians());
		setTurnGunRightRadians(swivel);
		execute();
		waitFor(new Condition("turnOver"){
			public boolean test(){
				return (getGunTurnRemaining() == 0);
			};
		});
		fire(bulletPower);
		
		if (e.getDistance() > 125) {
			setTurnRight(e.getBearing());
			setAhead(e.getDistance() - 50);
			scan();
			setTurnRadarRight(360);
			return;
		}

		if (e.getDistance() < 75) { 
			if (e.getBearing() > -90 && e.getBearing() <= 90) {
				setBack(30);
			} else {
				setAhead(30);
			}
		}
		scan();
		setTurnRadarRight(360);
	}

	public void onDeath(DeathEvent e){
		//revenge = lastBullet;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		lastBullet = e.getBullet().getName();
		/**
		target = e.getBullet().getName();
		out.println("Tracking " + target + " because they shot at me like a poo face");
	**/}
	

	public void onRobotDeath(RobotDeathEvent e) {	
		if (e.getName() == target){
			target = null;
		}
	}	
}
