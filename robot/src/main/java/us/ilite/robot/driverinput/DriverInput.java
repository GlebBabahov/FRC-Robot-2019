package us.ilite.robot.driverinput;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;
import com.team254.lib.util.Util;
import edu.wpi.first.wpilibj.Joystick;
import us.ilite.common.Data;
import us.ilite.common.config.DriveTeamInputMap;
import us.ilite.common.config.SystemSettings;
import us.ilite.common.types.ETrackingType;
import us.ilite.common.types.input.EInputScale;
import us.ilite.common.types.input.ELogitech310;
import us.ilite.robot.commands.Delay;
import us.ilite.robot.modules.*;
import us.ilite.robot.modules.Module;

public class DriverInput extends Module {

    protected static final double 
    DRIVER_SUB_WARP_AXIS_THRESHOLD = 0.5;
    private ILog mLog = Logger.createLog(DriverInput.class);

    protected final Drive driveTrain;
    protected final Superstructure mSuperstructure;

    private boolean mDriverStartedCommands;

    private Joystick mDriverJoystick;
    private Joystick mOperatorJoystick;

    protected Codex<Double, ELogitech310> mDriverInputCodex, mOperatorInputCodex;

    private Data mData;

    public DriverInput(Drive pDrivetrain, Superstructure pSuperstructure, Data pData, boolean pSimulated) {
        this.driveTrain = pDrivetrain;
        this.mSuperstructure = pSuperstructure;
        this.mData = pData;
        this.mDriverInputCodex = mData.driverinput;
        this.mOperatorInputCodex = mData.operatorinput;
        if(pSimulated) {
            // Use a different joystick library?

        } else {
            this.mDriverJoystick = new Joystick(0);
            this.mOperatorJoystick = new Joystick(1);
        }
    }

    public DriverInput(Drive pDrivetrain, Superstructure pSuperstructure, Data pData) {
        this(pDrivetrain, pSuperstructure, pData, false);
    }

    @Override
    public void modeInit(double pNow) {
    // TODO Auto-generated method stub
        mDriverStartedCommands = false;
    }

    @Override
    public void periodicInput(double pNow) {
        ELogitech310.map(mData.driverinput, mDriverJoystick);
        ELogitech310.map(mData.operatorinput, mOperatorJoystick);
    }

    @Override
    public void update(double pNow) {
        /*
        If we aren't already running commands and the driver is pressing a button that triggers a command,
        set the superstructure command queue based off of buttons
        */
        if(!mDriverStartedCommands && isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command start");
            mDriverStartedCommands = true;
            updateVisionCommands();
        /*
        If the driver started the commands that the superstructure is running and then released the button,
        stop running commands.
        */
        } else if(mSuperstructure.isRunningCommands() && mDriverStartedCommands && !isDriverAllowingTeleopCommands()) {
            mLog.warn("Requesting command stop: driver no longer allowing commands");
            mDriverStartedCommands = false;
            mSuperstructure.stopRunningCommands();
        } else if(mSuperstructure.isRunningCommands() && isAutoOverridePressed()) {
            mLog.warn("Requesting command stop: override pressed");
            mSuperstructure.stopRunningCommands();
        }

        // Teleop control
        if (!mSuperstructure.isRunningCommands()) {
            updateDriveTrain();
            updateAcquisition();
            updateScoring();
            updateElevator();
        } 

    }
    
    private void updateDriveTrain() {
        if (mData.driverinput.isSet(DriveTeamInputMap.DRIVER_THROTTLE_AXIS)
                && mData.driverinput.isSet(DriveTeamInputMap.DRIVER_TURN_AXIS)
                && mData.driverinput.isSet(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS)) {
            double rotate = mData.driverinput.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
            double throttle = -mData.driverinput.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);

            //		    throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
            rotate = EInputScale.EXPONENTIAL.map(rotate, 2);
            rotate = Util.limit(rotate, 0.7);

            if (mData.driverinput.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
                throttle *= SystemSettings.kSnailModePercentThrottleReduction;
                rotate *= SystemSettings.kSnailModePercentRotateReduction;
            }

            DriveMessage driveMessage = DriveMessage.fromThrottleAndTurn(throttle, rotate);
            driveMessage.setNeutralMode(NeutralMode.Brake);
            driveMessage.setControlMode(ControlMode.PercentOutput);

            driveTrain.setDriveMessage(driveMessage);
        }
    }

    private void updateSplitTriggerAxisFlip() {

        double rotate = mDriverInputCodex.get(DriveTeamInputMap.DRIVER_TURN_AXIS);
        double throttle = -mDriverInputCodex.get(DriveTeamInputMap.DRIVER_THROTTLE_AXIS);

        if(mDriverInputCodex.get(ELogitech310.RIGHT_TRIGGER_AXIS) > 0.3) {
            rotate = rotate;
            throttle = throttle;
        } else if(mDriverInputCodex.get(ELogitech310.LEFT_TRIGGER_AXIS) > 0.3) {
            throttle = -throttle;
            rotate = rotate;
        }

        rotate = Util.limit(rotate, SystemSettings.kDriverInputTurnMaxMagnitude);

		// throttle = EInputScale.EXPONENTIAL.map(throttle, 2);
        // rotate = Util.limit(rotate, 0.7);

        // if (mDriverInputCodex.get(DriveTeamInputMap.DRIVER_SUB_WARP_AXIS) > DRIVER_SUB_WARP_AXIS_THRESHOLD) {
        //     throttle *= SystemSettings.kSnailModePercentThrottleReduction;
        //     rotate *= SystemSettings.kSnailModePercentRotateReduction;
        // }

        DriveMessage driveMessage = DriveMessage.fromThrottleAndTurn(throttle, rotate);
        driveMessage.setNeutralMode(NeutralMode.Brake);
        driveMessage.setControlMode(ControlMode.PercentOutput);

        driveTrain.setDriveMessage(driveMessage);

    }

    private void updateAcquisition() {
        if(mOperatorInputCodex.get(DriveTeamInputMap.OPERATOR_INTAKE_GROUND_CARGO_AXIS) < -0.5) {
            mSuperstructure.requestIntaking(Superstructure.EAcquisitionState.GROUND_CARGO);
        } else if(mOperatorInputCodex.get(DriveTeamInputMap.OPERATOR_INTAKE_GROUND_HATCH_AXIS) < -0.5) {
            mLog.info("GROUND HATCH");

            mSuperstructure.requestIntaking(Superstructure.EAcquisitionState.GROUND_HATCH);
        }

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_HATCH_PUSH)) {
            mSuperstructure.requestScoring(Superstructure.EScoringState.HATCH);
        } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_CARGO_SPIT)) {
            mSuperstructure.requestScoring(Superstructure.EScoringState.CARGO);
        }

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_HATCH_EXTEND)) {
            mSuperstructure.extendHatchGrabber(true);
        } else {
            mSuperstructure.extendHatchGrabber(false);
        }

        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_HATCH_GRAB)) {
            mSuperstructure.grabHatch(true);
        } else {
            mSuperstructure.grabHatch(false);
        }
    }
    
    private void updateScoring() {
        
    }
    
    private void updateElevator() {
        if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_CARGO_BUTTON)) {
            if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_LEVEL_THREE)) {
                
            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_LEVEL_TWO)) {
                
            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_GROUND)) {
                
            }
        } else {
            if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_LEVEL_THREE)) {

            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_LEVEL_TWO)) {

            } else if(mOperatorInputCodex.isSet(DriveTeamInputMap.OPERATOR_ELEVATOR_GROUND)) {

            }
        }
    }

    /**
     * Commands the superstructure to start vision tracking depending on
     * button presses.
     */
    protected void updateVisionCommands() {

        ETrackingType trackingType = null;
        // Switch the limelight to a pipeline and track
        if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_TARGET_BTN)) {
            trackingType = ETrackingType.TARGET_LEFT;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_CARGO_BTN)) {
            trackingType = ETrackingType.CARGO_LEFT;
        } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_TRACK_HATCH_BTN)) {
            trackingType = ETrackingType.HATCH_LEFT;
        }

        // If the driver selected a tracking enum and we won't go out of bounds
        if(trackingType != null && trackingType.ordinal() < ETrackingType.values().length - 1) {
            int trackingTypeOrdinal = trackingType.ordinal();
            if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_LEFT)) {
                // If driver wants to seek left, we don't need to change anything
                trackingType = ETrackingType.values()[trackingTypeOrdinal];
            } else if(mDriverInputCodex.isSet(DriveTeamInputMap.DRIVER_NUDGE_SEEK_RIGHT)) {
                // If driver wants to seek right, switch from "_LEFT" enum to "_RIGHT" enum
                trackingType = ETrackingType.values()[trackingTypeOrdinal + 1];
            }
            mSuperstructure.stopRunningCommands();
            mSuperstructure.startCommands(new Delay(30)); // Placeholder
        }

    }

    public boolean isDriverAllowingTeleopCommands() {
        boolean runCommands = false;
        for(ELogitech310 l : SystemSettings.kTeleopCommandTriggers) {
            if(mDriverInputCodex.isSet(l)) {
                runCommands = true;
            }
        }
        return runCommands;
    }

    public boolean isAutoOverridePressed() {
        boolean autonOverride = false;
        for(ELogitech310 l : SystemSettings.kAutonOverrideTriggers) {
            if(mDriverInputCodex.isSet(l) && mDriverInputCodex.get(l) > SystemSettings.kAutonOverrideAxisThreshold) {
                autonOverride = true;
            }
        }
        return autonOverride;
    }

    @Override
    public void shutdown(double pNow) {
// TODO Auto-generated method stub

    }

}
