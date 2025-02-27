package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimConstant")
public class AimConstant extends Check implements RotationCheck {
    public AimConstant(GrimPlayer playerData) {
        super(playerData);
    }

    private double buffer = 0;
    private double decay;
    private int maxBuffer;
    private double maxDeltaRot, minDeltaRotAccel;
    private double lastDeltaX = 0, lastDeltaY = 0;

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        double deltaX = rotationUpdate.getDeltaXRot();
        double deltaY = rotationUpdate.getDeltaYRot();
        double deltaRot = Math.hypot(deltaX, deltaY);
        double lastDeltaRot = Math.hypot(lastDeltaX, lastDeltaY);
        double deltaRotAccel = Math.abs(deltaRot - lastDeltaRot);

        if(player.compensatedEntities.getSelf().getRiding() != null) {
            return; //Fix false positives in boats and other entities
        }

        if(Math.abs(rotationUpdate.getTo().getPitch()) == 90 || player.packetStateData.lastPacketWasTeleport) {
            return; //Ignore 90 and -90 pitch rotations and teleports
        }

        if (deltaRotAccel <= minDeltaRotAccel && deltaRot >= maxDeltaRot) {
            if (buffer++ > maxBuffer) {
                flagAndAlert("accel=" + deltaRotAccel + " rot=" + deltaRot);

            }
        } else {
            buffer = Math.max(0, buffer - decay);
        }

        lastDeltaX = deltaX;
        lastDeltaY = deltaY;
    }

    @Override
    public void reload(ConfigManager config) {
        maxBuffer = config.getIntElse(getConfigName() + ".buffer", 7);
        decay = config.getDoubleElse(getConfigName() + ".decay", 0.3);
        maxDeltaRot = config.getDoubleElse(getConfigName() + ".maxDeltaRot", 0.4D);
        minDeltaRotAccel = config.getDoubleElse(getConfigName() + ".minDeltaRotAccel", 0.0001D);
    }
}
