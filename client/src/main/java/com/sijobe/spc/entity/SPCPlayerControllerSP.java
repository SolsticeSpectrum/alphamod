package com.sijobe.spc.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.PlayerControllerSP;
import net.minecraft.src.PlayerHelper;

public class SPCPlayerControllerSP extends PlayerControllerSP {
   public PlayerHelper ph;

   public SPCPlayerControllerSP(Minecraft minecraft, PlayerHelper ph) {
      super(minecraft);
      this.ph = ph;
   }

   public float getBlockReachDistance() {
      return this.ph.reachdistance;
   }

   public void interactWithEntity(EntityPlayer entityplayer, Entity entity) {
      entityplayer.useCurrentItemOnEntity(entity);
   }

   public void attackEntity(EntityPlayer entityplayer, Entity entity) {
      entityplayer.attackTargetEntityWithCurrentItem(entity);
   }
}
