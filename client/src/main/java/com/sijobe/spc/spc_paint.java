package com.sijobe.spc;

import net.minecraft.src.Block;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.PlayerHelper;

public class spc_paint extends SPCPlugin {

   public int paintID = -1;
   public int paintDamage = 0;
   public boolean paint = false;

   private static int cooldown;


   public PlayerHelper ph;

   public spc_paint() {
      cooldown = 0;
   }


   public void setPlayerHelper(PlayerHelper ph) {
      this.ph = ph;

   }

   @Override
   public String getVersion() {
      return "1.0";
   }

   @Override
   public String getName() {
      return "Paint v1.1";
   }

   @Override
   public boolean handleCommand(String s[]) {

      String[] split = s;

      if (split[0].equalsIgnoreCase("trunksbomb")) {
         ph.sendMessage("trunksbomb is the greatest.");
         return true;
      } else if (split[0].equalsIgnoreCase("paint")) {

         System.out.println("Trying to paint..");

         if (paint && split.length == 1) {
            paint = false;
            paintID = -1;
            paintDamage = 0;
            ph.sendMessage("No longer painting.");
            return true;
         }

         if (split.length > 1)
         {
            paintID = ph.getBlockID(split[1]);
            System.out.println("Painting " + paintID);
            if ( paintID >= 0 )
            {
               paint = true;

               if (split.length > 2) {
                  try
                  {
                     paintDamage = Integer.parseInt(split[2]);
                     if (paintDamage < 0 || paintDamage > 15) {
                        ph.sendMessage("Damage value must be 0-15");
                        paintDamage = 0;
                     }

                  } catch (Exception e) {
                     ph.sendError("Not a valid damage value.");
                  }
               } else {
                  paintDamage = 0;
               }

               ph.sendMessage("Now painting " + 
                     (paintID == 0 ? "air" : Block.blocksList[paintID]
                                                              .getBlockName().substring(Block.blocksList[paintID].
                                                                    getBlockName().indexOf('.')+1)) +
                                                                    (paintDamage == 0 ? "." : " with damage " + paintDamage +
                                                                    ".")
                                                                    + " Left-click to replace clicked block, right click to"
                                                                    + " place new block.");
            } else {
               ph.sendError("Invalid block name or ID");
            }
            return true;
         } else {
            ph.sendError("Not enough parameters for paint command.");
         }

         return true;
      }


      return false;
   }

   @Override
   public void handleLeftClick(SPCObjectHit o) {

      if (paint) paintBlocks(o.blockx,o.blocky,o.blockz);
   }

   @Override
   public void handleRightClick(SPCObjectHit o) {
      if (paint) paintBlocksRightClick(o.blockx,o.blocky,o.blockz);
   }

   @Override
   public void handleLeftButtonDown(SPCObjectHit o) {

      if (paint) paintBlocks(o.blockx,o.blocky,o.blockz);

   }

   @Override
   public void handleRightButtonDown(SPCObjectHit o) {

      if (paint) paintBlocksRightClick(o.blockx,o.blocky,o.blockz);
   }

   @Override
   public void atUpdate() {
      if ( cooldown > 0 ) cooldown--;
   }

   public void paintBlocksRightClick(int i, int j, int k)
   {
      if (cooldown > 0 )
         return;

      cooldown = 3;

      MovingObjectPosition mop = ph.ep.rayTrace(1024D, 1F);

      int side = mop.sideHit;
      if ( side == 0 ) j--;
      if ( side == 1 ) j++;
      if ( side == 2 ) k--;
      if ( side == 3 ) k++;
      if ( side == 4 ) i--;
      if ( side == 5 ) i++;

      paintBlocks(i,j,k);


   }
   public void paintBlocks(int i, int j, int k)
   {
      if (ph.ep.worldObj.getBlockId(i, j, k) != Block.bedrock.blockID)
         ph.ep.worldObj.setBlockWithNotify(i, j, k, paintID);
      ph.ep.worldObj.setBlockAndMetadataWithNotify(i, j, k, paintID, paintDamage);
   }
}



