package ac.grim.grimac.checks.impl.movement;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;

import java.math.BigDecimal;
import java.math.RoundingMode;

@CheckData(name = "FullStopA")
public class FullStopA extends Check implements PacketCheck {

    public FullStopA(GrimPlayer player) {
        super(player);
    }

    public Vector3d positionPrevious;
    public Vector3d delta = new Vector3d();

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);

            Vector3d pos = packet.getPosition();
            if(positionPrevious != null) {
                Vector3d deltaCurrent = positionPrevious.subtract(pos);

                if(delta != null && !player.isSneaking && player.bukkitPlayer.getLocation().clone().subtract(0, 1, 0).getBlock().isEmpty()) {
                    if((Math.abs(delta.x) > 0.01 && Math.abs(deltaCurrent.x) < 0.001) || (Math.abs(delta.z) > 0.01 && Math.abs(deltaCurrent.z) < 0.001)) {
                        flagAndAlert("prev=" + roundToDecimals(delta.getX(), 4) + "x," + roundToDecimals(delta.getZ(), 4) + "z stop=" + roundToDecimals(deltaCurrent.getX(), 4) + "x," + roundToDecimals(deltaCurrent.getZ(), 4) + "z");
                        setbackIfAboveSetbackVL();
                    }
                }

                delta = deltaCurrent;
            }

            positionPrevious = pos;
        } else if(event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
            WrapperPlayClientTabComplete packet = new WrapperPlayClientTabComplete(event);

            System.out.println(packet.getText());
        }
    }

    public static double roundToDecimals(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Decimal places cannot be negative");
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
