package us.ilite.robot.auto.paths.right;

import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.geometry.Translation2d;
import us.ilite.common.Data;
import us.ilite.common.lib.trajectory.TrajectoryGenerator;
import us.ilite.lib.drivers.VisionGyro;
import us.ilite.robot.auto.paths.AutoSequence;
import us.ilite.robot.auto.paths.FieldElementLocations;
import us.ilite.robot.auto.paths.RobotDimensions;
import us.ilite.robot.auto.paths.StartingPoses;
import us.ilite.robot.commands.ICommand;
import us.ilite.robot.modules.*;

import java.util.Arrays;
import java.util.List;

public class RightToRocketToRocket extends AutoSequence {

    public static final Pose2d kStartToBackRocket = new Pose2d(FieldElementLocations.kRocketRightHatch.translateBy(new Translation2d(RobotDimensions.kFrontToCenter, 0.0).rotateBy(Rotation2d.fromDegrees(35.0))), Rotation2d.fromDegrees(210.0));
    public static final Pose2d kBackRocketToLine = new Pose2d(FieldElementLocations.kRocketRightHatch.translateBy(new Translation2d(RobotDimensions.kFrontToCenter + 36.0, -12.0).rotateBy(Rotation2d.fromDegrees(35.0))), Rotation2d.fromDegrees(0.0));
    public static final Pose2d kLineToLoadingStationMidpoint = new Pose2d(218.0, 61.0, Rotation2d.fromDegrees(180.0));
    public static final Pose2d kLineToLoadingStation = new Pose2d(FieldElementLocations.kLoadingStation.translateBy(new Translation2d(RobotDimensions.kFrontToCenter, 0.0)), Rotation2d.fromDegrees(180.0));
    public static final Pose2d kLoadingStationToBackRocket = new Pose2d(kStartToBackRocket.getTranslation(), kStartToBackRocket.getRotation().rotateBy(Rotation2d.fromDegrees(180.0)));

    public static final List<Pose2d> kStartToBackRocketPath = Arrays.asList(
            StartingPoses.kFarSideStart,
            kStartToBackRocket
    );

    public static final List<Pose2d> kBackRocketToLinePath = Arrays.asList(
            new Pose2d(kStartToBackRocket.getTranslation(), kStartToBackRocket.getRotation().rotateBy(Rotation2d.fromDegrees(180.0))),
            kBackRocketToLine
    );

    public static final List<Pose2d> kLineToLoadingStationPath = Arrays.asList(
            new Pose2d(kBackRocketToLine.getTranslation(), kBackRocketToLine.getRotation().rotateBy(Rotation2d.fromDegrees(180.0))),
            kLineToLoadingStationMidpoint,
            kLineToLoadingStation
    );

    public static final List<Pose2d> kLoadingStationToBackRocketPath = Arrays.asList(
            new Pose2d(kLineToLoadingStation.getTranslation(), kLineToLoadingStation.getRotation().rotateBy(Rotation2d.fromDegrees(180.0))),
            new Pose2d(kLineToLoadingStationMidpoint.getTranslation(), kLineToLoadingStation.getRotation().rotateBy(Rotation2d.fromDegrees(180.0))),
            kBackRocketToLine
    );

    public static final List<Pose2d> kLineToBackRocketPath = Arrays.asList(
            new Pose2d(kBackRocketToLine.getTranslation(), kBackRocketToLine.getRotation().rotateBy(Rotation2d.fromDegrees(180.0))),
            kStartToBackRocket
    );

    public RightToRocketToRocket(TrajectoryGenerator mTrajectoryGenerator, Data mData, Drive mDrive, HatchFlower mHatchFlower, PneumaticIntake mPneumaticIntake, CargoSpit mCargoSpit, Elevator mElevator, Limelight mLimelight, VisionGyro mVisionGyro) {
        super(mTrajectoryGenerator, mData, mDrive, mHatchFlower, mPneumaticIntake, mCargoSpit, mElevator, mLimelight, mVisionGyro);
    }

    @Override
    public ICommand[] generateCargoSequence() {
        return new ICommand[0];
    }

    @Override
    public ICommand[] generateHatchSequence() {
        return new ICommand[0];
    }

}
