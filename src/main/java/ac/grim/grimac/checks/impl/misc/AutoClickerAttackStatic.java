package ac.grim.grimac.checks.impl.misc;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;


@CheckData(name = "AutoClicker (Timing)")
public class AutoClickerAttackStatic extends Check implements PacketCheck {

    public AutoClickerAttackStatic(GrimPlayer player) {
        super(player);
    }

    private long lastTime = 0;
    private final EvictingQueue<Long> timings = new EvictingQueue<>(20);

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {

        final boolean isAnimation = event.getPacketType() == PacketType.Play.Client.ANIMATION;

        if(isAnimation || event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {

            final long time = event.getTimestamp();

            if (isAnimation) {
                final long delta = time - lastTime;

                if (delta > 1000) {
                    timings.clear();
                }

                lastTime = time;
                timings.add(delta);
            } else {
                WrapperPlayClientInteractEntity i = new WrapperPlayClientInteractEntity(event);
                if (i.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                    if (timings.size() > 5) {
                        long referenceTime = timings.get(0);
                        int consistentCount = 0;

                        for (long timing : timings) {
                            long difference = Math.abs(timing - referenceTime);
                            if (difference <= 10) {
                                consistentCount++;
                            }
                        }

                        boolean allConsistent = consistentCount == timings.size();
                        boolean majorityConsistent = consistentCount >= (timings.size() / 2);

                        boolean flagDelta = time - lastTime < 2;

                        if (allConsistent) {
                            flagWithSetback();
                        } else {
                            flag();
                        }

                        if (majorityConsistent || flagDelta) {
                            alert("delta=" + (time - lastTime) + (flagDelta ? " (invalid)" : "") + " references=" + timings.size() + " consistency=" + consistentCount);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reload() {
        super.reload();
    }
}
