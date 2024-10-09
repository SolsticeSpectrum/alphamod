package com.sijobe.spc;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.PlayerHelper;

/**
 * The plugin class for WorldEdit. It uses the WorldEditPlugin class to load
 * all the interfaces and variables found in WorldEdit.jar
 * 
 * @author simo_415
 * Copyright (C) 2010-2011 simo_415 - (http://bit.ly/spcmod)  
 * 
 *  This file is part of Single Player Commands.
 *
 *  Single Player Commands is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Single Player Commands is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Single Player Commands. If not, see <http://www.gnu.org/licenses/>.
 */
public class spc_WorldEdit extends SPCPlugin {
   public int blockrightx;
   public int blockrighty;
   public int blockrightz;
   public int blockleftx;
   public int blocklefty;
   public int blockleftz;

   public static File WORLDEDITJAR = new File(Minecraft.getMinecraftDir(),"bin/WorldEdit.jar");
   public static File RHINOJAR = new File(Minecraft.getMinecraftDir(),"bin/rhino.jar");

   public static boolean CANINITIALISE = true;

   public WorldEditPlugin WEP;

   static {
      if (!WORLDEDITJAR.exists()) { // Attempt to extract it from Minecraft.jar as noobs can't read instructions
         PlayerHelper.extractFile(new File(Minecraft.getMinecraftDir(),"bin/Minecraft.jar"), "WorldEdit.jar", new File(Minecraft.getMinecraftDir(),"bin"));
      }
      if (!PlayerHelper.addToClasspath(WORLDEDITJAR)) {
         CANINITIALISE = false;
      }
      PlayerHelper.addToClasspath(RHINOJAR);
   }

   /**
    * @throws Exception If WorldEdit cannot be initialised. This is generally
    * caused by WorldEdit.jar not being in the correct location
    */
   public spc_WorldEdit() throws Exception { 
      if (!CANINITIALISE) {
         throw new Exception("Check WorldEdit.jar is in the correct location");
      }
      if (WEP == null) {
         WEP = new WorldEditPlugin();
      }
   }
   
   /**
    * Overwrites default behavior to intercept call and update the WorldEdit
    * player interface
    * @see SPCPlugin#setPlayerHelper(net.minecraft.src.PlayerHelper)
    */
   public void setPlayerHelper(PlayerHelper ph) {
      super.setPlayerHelper(ph);
      WEP.setPlayer(ph.ep);
   }

   /**
    * Processes the command that the user issued.
    * @param args The arguments that the user issued.
    * @return If the command was found true is returned
    */
   @Override
   public boolean handleCommand(String args[]) {
      try {
         if ((Boolean)WEP.getHandleCommand().invoke(WEP.getController(), new Object[]{WEP.getPlayer(),args.clone()})) {
            return true;
         } else {
            return false;
         }
      } catch (Throwable t) {
         t.printStackTrace();
         return false;
      }
   }

   /**
    * Handle left click
    */
   @Override
   public void handleLeftButtonDown(SPCObjectHit o) {
      if (o == null || o.blocky < 0) {
         return;
      }
      try {
         WEP.getHandleArmSwing().invoke(WEP.getController(), new Object[]{WEP.getPlayer()});
         if ((o.blockx != blockleftx || o.blocky != blocklefty || o.blockz != blockleftz) && (o.blocky > -1)) {
            Object vector = WEP.getWorldvector().newInstance(WEP.getPlayer().getWorld(),o.blockx,o.blocky,o.blockz);
            WEP.getHandleBlockLeftClick().invoke(WEP.getController(), new Object[]{WEP.getPlayer(),vector});
            blockleftx = o.blockx;
            blocklefty = o.blocky;
            blockleftz = o.blockz;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Handle right click
    * @throws Exception is thrown when the command was unable to run
    */
   @Override
   public void handleRightButtonDown(SPCObjectHit o) {
      if (o == null || o.blocky < 0) {
         return;
      }
      try {
         WEP.getHandleRightClick().invoke(WEP.getController(), new Object[]{WEP.getPlayer()});
         if ((o.blockx != blockrightx || o.blocky != blockrighty || o.blockz != blockrightz) && (o.blocky > -1)) {
            Object vector = WEP.getWorldvector().newInstance(WEP.getPlayer().getWorld(),o.blockx,o.blocky,o.blockz);
            WEP.getHandleBlockRightClick().invoke(WEP.getController(), new Object[]{WEP.getPlayer(),vector});
            blockrightx = o.blockx;
            blockrighty = o.blocky;
            blockrightz = o.blockz;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public String getVersion() {
      return "1.3";
   }

   @Override
   public String getName() {
      return "WorldEditPlugin";
   }
}
