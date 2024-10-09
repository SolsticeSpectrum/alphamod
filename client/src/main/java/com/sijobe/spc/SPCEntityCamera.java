package com.sijobe.spc;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.Session;
import net.minecraft.src.World;

public class SPCEntityCamera extends EntityPlayerSP {

   public SPCEntityCamera(Minecraft mc, World world, Session s, int i) {
      super(mc, world, s, i);
      yOffset = 1.62F;
   }

   @Override
   public boolean canBePushed() {
      return false;
   }
   
   @Override
   public void onEntityUpdate() {
   }
   
   @Override
   public void onUpdate() {
      
   }
   
   @Override
   public void onDeath(Entity entity) {
   }
   
   @Override
   public boolean isEntityAlive() {
      return true;
   }
   
   public void setCamera(double x, double y, double z, float yaw, float pitch) {
      lastTickPosX = posX;
      lastTickPosY = posY;
      lastTickPosZ = posZ;
      posX += x;
      posY += y;
      posZ += z;
      prevRotationYaw = rotationYaw;
      prevRotationPitch = rotationPitch;
      rotationYaw = yaw;
      rotationPitch = pitch;
   }
}
