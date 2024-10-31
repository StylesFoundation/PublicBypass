public class Scaffold {

    private final List<Packet<?>> packets = new ArrayList<>();

    public void onDisable() {
        if (!packets.isEmpty()) {
            packets.forEach(mc.getNetHandler().getNetworkManager()::sendPacketNoEvent);
            packets.clear();
        }

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        if (event.isPost()) return;
        if (PlayerUtil.getLastDistance() > .22 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.onGround) {
            final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            final double multiplier = .5 - PlayerUtil.getSpeedEffect() * .05;
            final double random = Math.random() * .007;
            event.setX(event.getX() - xDist * (multiplier + random));
            event.setZ(event.getZ() - zDist * (multiplier + random));
            event.setY(event.getY() + 1E-8);
        }
    }

    @Subscribe
    public void onMotion(MotionEvent event) { // you can also put this code into the TickEvent.
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - PlayerUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - PlayerUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
        }

        if (mc.thePlayer.ticksExisted % 2 != 0 && !packets.isEmpty()) {
            packets.forEach(mc.getNetHandler().getNetworkManager()::sendPacketNoEvent);
            packets.clear();
        }
    }

    @Subscribe
    public void onPacket(PacketEvent event) {
        if (event.isReceiving()) return;
        if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0
                && (event.getPacket() instanceof C08PacketPlayerBlockPlacement
                || event.getPacket() instanceof C0APacketAnimation
                || event.getPacket() instanceof C09PacketHeldItemChange)) {
            packets.add(event.getPacket());
            event.setCancelled(true);
        }
    }
}

// Utils
public static double getLastDistance() {
    final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
    final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
    return Math.sqrt(xDist * xDist + zDist * zDist);
}

public static int getSpeedEffect() {
    return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
}