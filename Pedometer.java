package GarmBox;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.lang.Math;
import static robocode.util.Utils.*;

public class Pedometer extends TeamRobot
{
	double rev = 0;
	boolean fire = false;
	int fC = 0;
	String target = null;
	double bulletPower = 2;
	double swivel;
	int misses = 0;
	static int udt = 0;

	static double[][] r = new double[1][4];
	static double[][] s = new double[1][4];
	static double[][] j = new double[][] {{1,0,0,0}, {-11.0/6, 3, -1.5, 1.0/3}, {1, -2.5, 2, -.5}, {-1.0/6, .5, -.5, 1.0/6}};
		
	public void run() {
		setColors(new Color(41, 140, 227), new Color(115, 49, 181), new Color(201, 50, 194));
		setBulletColor(new Color(0,0,0));
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		while(true) {
			if (target == null) {
				setTurnRadarRight(360);
				while (getRadarTurnRemaining() > 0) {
					execute();
				}
			}
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

		if (target == null){ 
			target = e.getName();
			out.println("Tracking " + target);
		}

		double myX = getX();
		double myY = getY();
		double absBearing = Math.toRadians(getHeading() + e.getBearing());
		double enemyHeading = e.getHeadingRadians();
		double enemyX = myX + e.getDistance()*Math.sin(absBearing);
		double enemyY = myY + e.getDistance()*Math.cos(absBearing);
		double enemyVelocity = e.getVelocity();
		double battleFieldHeight = getBattleFieldHeight();
		double battleFieldWidth = getBattleFieldWidth();

		updt(enemyX, enemyY);

		double[][] pro = multiply(j, r);
		double[][] proy = multiply(j, s);

		int n = Math.max(udt - 1, 1);
		int ticks = 0;
		double predictedX = pro[0][0] + pro[1][0]*(n) + pro[2][0]*(n*n) + pro[3][0]*(n*n*n);
		double predictedY = proy[0][0] + proy[1][0]*(n) + proy[2][0]*(n*n) + proy[3][0]*(n*n*n);

		while ((++ticks) * (20 - 3 * (udt-1)) < Point2D.Double.distance(myX, myY, predictedX, predictedY)){
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			
			if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0){
				predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
				break;
			}
		}

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
		fire(udt-1);

		if (e.getDistance() > 175) {
			setTurnRight(e.getBearing());
			setAhead(e.getDistance() - 50);
			scan();
			setTurnRadarRight(360);
			return;
		}

		if (e.getDistance() < 125) { 
			if (e.getBearing() > -90 && e.getBearing() <= 90) {
				setBack(30);
			} else {
				setAhead(30);
			}
		}
		scan();
		setTurnRadarRight(360);
		execute();
	}

	public void onBulletMissed(BulletMissedEvent e) {
		if (misses > 5) {
			misses = 0;
			r = new double[1][4];
			s = new double[1][4];
			udt = 0;
		}
	}

	public void onRobotDeath(RobotDeathEvent e) {	
		if (e.getName() == target){
			target = null;
			r = new double[1][4];
			s = new double[1][4];
			udt = 0;
			misses = 0;
		}
	}

	public static double[][] multiply(double[][] matrix, double[][] values){
		double[][] res = new double[matrix.length][1];
		for (int q = 0; q < matrix.length; q++) {
			for (int q2 = 0; q2 < matrix.length; q2++) {
				res[q][0] = 0;
			}
		}
		for (int q = 0; q < matrix.length; q++) {
			for (int q2 = 0; q2 < matrix.length; q2++) {
				res[q][0] = res[q][0] + matrix[q][q2] * values[0][q2];
			}
		}
		return res;
	}

	public static void updt(double x, double y) {
		if (udt == 4) {
			for (int i = 1; i < 4; i++){
				r[0][i - 1] = r[0][i];
				s[0][i - 1] = s[0][i];
			}
			r[0][3] = x;
			s[0][3] = y;
		} else {
			r[0][udt] = x;
			s[0][udt] = y;
			udt++;
		}
	}

}
