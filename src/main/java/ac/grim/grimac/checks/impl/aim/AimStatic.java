package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimStatic")
public class AimStatic extends Check implements RotationCheck {
    public AimStatic(GrimPlayer playerData) {
        super(playerData);
    }

    private double buffer = 0;
    private double decay;
    private int maxBuffer;

    private double minDeltaY, maxDeltaY;
    private double minDeltaX, maxDeltaX;

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        double deltaX = rotationUpdate.getDeltaXRotABS();
        double deltaY = rotationUpdate.getDeltaYRotABS();

        if(player.compensatedEntities.getSelf().getRiding() != null) {
            return; //Fix false positives in boats and other entities
        }

        if(Math.abs(rotationUpdate.getTo().getPitch()) == 90 || player.packetStateData.lastPacketWasTeleport) {
            return; //Ignore 90 and -90 pitch rotations and teleports
        }
        if (deltaX <= minDeltaX && deltaY >= maxDeltaY || deltaY <= minDeltaY && deltaX >= maxDeltaX) {
            if (buffer++ > maxBuffer) {
                flagAndAlert("deltaX=" + deltaX + " deltaY=" + deltaY);

            }
        } else {
            buffer = Math.max(0, buffer - decay);
        }
    }

    @Override
    public void reload(ConfigManager config) {
        maxBuffer = config.getIntElse(getConfigName() + ".buffer", 7);
        decay = config.getDoubleElse(getConfigName() + ".decay", 1);
        minDeltaX = config.getDoubleElse(getConfigName() + ".maxDeltaX", 0.0001D);
        maxDeltaX = config.getDoubleElse(getConfigName() + ".maxDeltaX", 100D);
        minDeltaY = config.getDoubleElse(getConfigName() + ".maxDeltaY", 0.0001D);
        maxDeltaY = config.getDoubleElse(getConfigName() + ".maxDeltaY", 100D);
    }
}
