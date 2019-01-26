package us.ilite.robot.commands;

import com.team254.lib.util.Util;
import us.ilite.robot.modules.Drive;
import us.ilite.robot.modules.DriveMessage;

public class MotionToAbsoluteDistanceCommand implements ICommand {
    private final Drive drive;
    private final IAbsoluteDistanceProvider distanceProvider;
    private final double goalDistanceInInches;

    
    public MotionToAbsoluteDistanceCommand(Drive drive, IAbsoluteDistanceProvider distanceProvider, double goalDistanceInInches) {
        this.distanceProvider = distanceProvider;
        this.drive = drive;
        this.goalDistanceInInches = goalDistanceInInches;
    }
    @Override
    public void init(double pNow) {
    }

    @Override
    public void shutdown(double pNow) {
        
    }

    @Override
    public boolean update(double pNow) {
        boolean isFinished = false;
        //Take a measurment of how far the distance provider
        double deltaDistance = distanceProvider.getAbsoluteDistanceInInches() - goalDistanceInInches;
        if(Util.epsilonEquals(deltaDistance, 0.01)) {
            isFinished = true;
        } else if(deltaDistance > 0.0d) {
            //Move backwards
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(-1.0, 0));
            isFinished = false;
        } else if(deltaDistance < 0.0d) {
            drive.setDriveMessage(DriveMessage.fromThrottleAndTurn(1.0, 0));
        } else {
            //Should be impossible, but we'll say we're done
            isFinished = true;
        }
        
        return isFinished;
    }

}