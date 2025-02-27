package ac.grim.grimac.checks.impl.movement;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;

@CheckData(name = "Baritone Movement")
public class BaritoneMovement extends Check implements PostPredictionCheck {

    private double lastX = 0, lastY = 0, lastZ = 0;
    private long lastTime = 0;
    private static final double PERFECT_MOVEMENT_THRESHOLD = 0.001; // Movement threshold for "perfect" movement
    private static final long MOVEMENT_TIMEOUT = 1000 * -1; // Time threshold to consider movement in a line

    public BaritoneMovement(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);

            Vector3d pos = packet.getPosition();

            double x = pos.x;
            double y = pos.y;
            double z = pos.z;

            long time = event.getTimestamp();
            long delta = time - lastTime;

            // Check if player has moved within defined threshold
            if (lastTime > 0 && delta < MOVEMENT_TIMEOUT) {
                double deltaX = x - lastX;
                double deltaY = y - lastY;
                double deltaZ = z - lastZ;

                // System.out.println(deltaX + " " + deltaY + " " + deltaZ);

                if(deltaX < 0.2 && deltaY < 0.2 && deltaZ < 0.2) {
                    return;
                }

                boolean xNoZ = Math.abs(deltaX) > PERFECT_MOVEMENT_THRESHOLD && Math.abs(deltaZ) < PERFECT_MOVEMENT_THRESHOLD;
                boolean zNoX = Math.abs(deltaZ) > PERFECT_MOVEMENT_THRESHOLD && Math.abs(deltaX) < PERFECT_MOVEMENT_THRESHOLD;
                boolean diagonalPerfect = Math.abs(deltaX) - Math.abs(deltaZ) < PERFECT_MOVEMENT_THRESHOLD;

                // If movement is perfect in any of the considered ways, flag it
                if ((xNoZ ^ zNoX) || diagonalPerfect) {
                    if(flagWithSetback()) {
                        alert("xDelta=%s zDelta=%s".formatted(deltaX, deltaZ));
                    }
                } else {
                    reward();
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;

            lastTime = time;
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        PostPredictionCheck.super.onPredictionComplete(predictionComplete);
    }
}
