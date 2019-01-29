package us.ilite.common.types.sensor;

import com.flybotix.hfr.codex.CodexOf;

public enum EGyro implements CodexOf<Double> {

    YAW_DEGREES, PITCH_DEGREES, ROLL_DEGREES,
    YAW_RATE_DEGREES, PITCH_RATE_DEGREES, ROLL_RATE_DEGREES,
    ACCEL_X, ACCEL_Y, ACCEL_Z,

}
