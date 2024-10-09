package net.minecraft.src;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextPane;

import com.sijobe.spc.SPCEntityCamera;
import com.sijobe.spc.SPCVersion;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;

public class EntityPlayerSP extends EntityPlayer
{
   public MovementInput movementInput;
   protected Minecraft mc;

   /**
    * Used to tell if the player pressed forward twice. If this is at 0 and it's pressed (And they are allowed to
    * sprint, aka enough food on the ground etc) it sets this to 7. If it's pressed and it's greater than 0 enable
    * sprinting.
    */
   protected int sprintToggleTimer;

   /** Ticks left before sprinting is disabled. */
   public int sprintingTicksLeft;
   public float renderArmYaw;
   public float renderArmPitch;
   public float prevRenderArmYaw;
   public float prevRenderArmPitch;
   private MouseFilter field_21903_bJ;
   private MouseFilter field_21904_bK;
   private MouseFilter field_21902_bL;

   public PlayerHelper ph;
   public boolean multiplayer;
   public boolean phexists;
   public static Object MESSAGESHOWN;
   public static Object STARTUP;
   public String curmcversion;
   public static final String MCVERSION = "b1.7.3";
   public static final SPCVersion SPCVERSION = new SPCVersion("Single Player Commands","3.2.2",new Date(1333630063890L)); // 2012-04-05 22:47:43
   public Vector<String> missingRequiredClasses;
   public Vector<String> missingOptionalClasses;

   public EntityPlayerSP(Minecraft minecraft, World world, Session session, int i) {
      super(world);
      this.mc = minecraft;
      this.dimension = i;
      if (session != null && session.username != null && session.username.length() > 0) {
         this.skinUrl = "http://s3.amazonaws.com/MinecraftSkins/" + session.username + ".png";
      }

      username = session.username;
      initPlayerHelper(session);
      phexists = true;
   }

   /**
    * Tries to moves the entity by the passed in displacement. Args: x, y, z
    */
   public void moveEntity(double var1, double var3, double var5) {
      if (canRunSPC() && ph.moveplayer && !ph.movecamera && mc.renderViewEntity instanceof SPCEntityCamera) {
         ((SPCEntityCamera)mc.renderViewEntity).setCamera(0, 0, 0,ph.freezecamyaw, ph.freezecampitch);
      } else if (canRunSPC() && ph.noClip) {
         posX += var1;
         posY += var3;
         posZ += var5;
         return;
      } else if (canRunSPC() && mc.renderViewEntity instanceof SPCEntityCamera) {
         ((SPCEntityCamera)mc.renderViewEntity).setCamera(var1, var3, var5, rotationYaw, rotationPitch);
         return;
      }
      super.moveEntity(var1, var3, var5);
   }

   public void updatePlayerActionState() {
      super.updatePlayerActionState();
      this.moveStrafing = this.movementInput.moveStrafe;
      this.moveForward = this.movementInput.moveForward;
      this.isJumping = this.movementInput.jump;
   }

   /**
    * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
    * use this to react to sunlight and start to burn.
    */
   public void onLivingUpdate() {
      if(!this.mc.statFileWriter.hasAchievementUnlocked(AchievementList.openInventory)) {
         this.mc.guiAchievement.queueAchievementInformation(AchievementList.openInventory);
      }

      this.prevTimeInPortal = this.timeInPortal;
      if(this.inPortal) {
         if(!this.worldObj.multiplayerWorld && this.ridingEntity != null) {
            this.mountEntity((Entity)null);
         }

         if(this.mc.currentScreen != null) {
            this.mc.displayGuiScreen((GuiScreen)null);
         }

         if(this.timeInPortal == 0.0F) {
            this.mc.sndManager.playSoundFX("portal.trigger", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
         }

         this.timeInPortal += 0.0125F;
         if(this.timeInPortal >= 1.0F) {
            this.timeInPortal = 1.0F;
            if(!this.worldObj.multiplayerWorld) {
               this.timeUntilPortal = 10;
               this.mc.sndManager.playSoundFX("portal.travel", 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
               this.mc.usePortal();
            }
         }

         this.inPortal = false;
      } else {
         if(this.timeInPortal > 0.0F) {
            this.timeInPortal -= 0.05F;
         }

         if(this.timeInPortal < 0.0F) {
            this.timeInPortal = 0.0F;
         }
      }

      if(this.timeUntilPortal > 0) {
         --this.timeUntilPortal;
      }

      this.movementInput.updatePlayerMoveState(this);
      if(this.movementInput.sneak && this.ySize < 0.2F) {
         this.ySize = 0.2F;
      }

      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, this.boundingBox.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      super.onLivingUpdate();
   }

   public void resetPlayerKeyState() {
      this.movementInput.resetKeyState();
   }

   public void handleKeyPress(int var1, boolean var2) {
      this.movementInput.checkKeyForMovementInput(var1, var2);
   }

   /**
    * (abstract) Protected helper method to write subclass entity data to NBT.
    */
   public void writeEntityToNBT(NBTTagCompound var1)
   {
      super.writeEntityToNBT(var1);
      var1.setInteger("Score", score);
      if (canRunSPC()) {
         ph.writeWaypointsToNBT(((SaveHandler) this.mc.theWorld.saveHandler).getSaveDirectory());
      }
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readEntityFromNBT(NBTTagCompound var1)
   {
      super.readEntityFromNBT(var1);
      score = var1.getInteger("Score");
      if (canRunSPC()) {
         ph.readWaypointsFromNBT(((SaveHandler) this.mc.theWorld.saveHandler).getSaveDirectory());
      }
   }

   /**
    * sets current screen to null (used on escape buttons of GUIs)
    */
   public void closeScreen() {
      super.closeScreen();
      this.mc.displayGuiScreen((GuiScreen)null);
   }

   /**
    * Displays the GUI for editing a sign. Args: tileEntitySign
    */
   public void displayGUIEditSign(TileEntitySign var1) {
      this.mc.displayGuiScreen(new GuiEditSign(var1));
   }

   /**
    * Displays the GUI for interacting with a chest inventory. Args: chestInventory
    */
   public void displayGUIChest(IInventory var1) {
      this.mc.displayGuiScreen(new GuiChest(this.inventory, var1));
   }

   /**
    * Displays the crafting GUI for a workbench.
    */
   public void displayWorkbenchGUI(int var1, int var2, int var3) {
      this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj, var1, var2, var3));
   }

   /**
    * Displays the furnace GUI for the passed in furnace entity. Args: tileEntityFurnace
    */
   public void displayGUIFurnace(TileEntityFurnace var1) {
      this.mc.displayGuiScreen(new GuiFurnace(this.inventory, var1));
   }

   /**
    * Displays the dipsenser GUI for the passed in dispenser entity. Args: TileEntityDispenser
    */
   public void displayGUIDispenser(TileEntityDispenser var1) {
      this.mc.displayGuiScreen(new GuiDispenser(this.inventory, var1));
   }

   /**
    * Called whenever an item is picked up from walking over it. Args: pickedUpEntity, stackSize
    */
   public void onItemPickup(Entity var1, int var2) {
      this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.theWorld, var1, this, -0.5F));
   }

   public int getPlayerArmorValue() {
      return this.inventory.getTotalArmorValue();
   }

   /**
    * Sends a chat message from the player. Args: chatMessage
    */
   public void sendChatMessage(String s) {
      if (canRunSPC()) {
         ph.processCommand(s);
      }

   }

   /**
    * Returns if this entity is sneaking.
    */
   public boolean isSneaking() {
      return this.movementInput.sneak && !this.sleeping;
   }

   /**
    * Updates health locally.
    */
   public void setHealth(int var1) {
      int var2 = this.health - var1;
      if(var2 <= 0) {
         this.health = var1;
         if(var2 < 0) {
            this.heartsLife = this.heartsHalvesLife / 2;
         }
      } else {
         this.field_9346_af = var2;
         this.prevHealth = this.health;
         this.heartsLife = this.heartsHalvesLife;
         this.damageEntity(var2);
         this.hurtTime = this.maxHurtTime = 10;
      }

   }

   public void respawnPlayer() {
      this.mc.respawn(false, 0);
   }

   public void func_6420_o() {
   }

   /**
    * Add a chat message to the player
    */
   public void addChatMessage(String var1) {
      this.mc.ingameGUI.addChatMessageTranslate(var1);
   }

   /**
    * Adds a value to a statistic field.
    */
   public void addStat(StatBase var1, int var2) {
      if(var1 != null) {
         if(var1.func_25067_a()) {
            Achievement var3 = (Achievement)var1;
            if(var3.parentAchievement == null || this.mc.statFileWriter.hasAchievementUnlocked(var3.parentAchievement)) {
               if(!this.mc.statFileWriter.hasAchievementUnlocked(var3)) {
                  this.mc.guiAchievement.queueTakenAchievement(var3);
               }

               this.mc.statFileWriter.readStat(var1, var2);
            }
         } else {
            this.mc.statFileWriter.readStat(var1, var2);
         }

      }
   }

   private boolean isBlockTranslucent(int var1, int var2, int var3) {
      return this.worldObj.isBlockNormalCube(var1, var2, var3);
   }


   /**
    * Adds velocity to push the entity out of blocks at the specified x, y, z position Args: x, y, z
    */
   protected boolean pushOutOfBlocks(double var1, double var3, double var5) {
      int var7 = MathHelper.floor_double(var1);
      int var8 = MathHelper.floor_double(var3);
      int var9 = MathHelper.floor_double(var5);
      double var10 = var1 - (double)var7;
      double var12 = var5 - (double)var9;
      if(this.isBlockTranslucent(var7, var8, var9) || this.isBlockTranslucent(var7, var8 + 1, var9)) {
         boolean var14 = !this.isBlockTranslucent(var7 - 1, var8, var9) && !this.isBlockTranslucent(var7 - 1, var8 + 1, var9);
         boolean var15 = !this.isBlockTranslucent(var7 + 1, var8, var9) && !this.isBlockTranslucent(var7 + 1, var8 + 1, var9);
         boolean var16 = !this.isBlockTranslucent(var7, var8, var9 - 1) && !this.isBlockTranslucent(var7, var8 + 1, var9 - 1);
         boolean var17 = !this.isBlockTranslucent(var7, var8, var9 + 1) && !this.isBlockTranslucent(var7, var8 + 1, var9 + 1);
         byte var18 = -1;
         double var19 = 9999.0D;
         if(var14 && var10 < var19) {
            var19 = var10;
            var18 = 0;
         }

         if(var15 && 1.0D - var10 < var19) {
            var19 = 1.0D - var10;
            var18 = 1;
         }

         if(var16 && var12 < var19) {
            var19 = var12;
            var18 = 4;
         }

         if(var17 && 1.0D - var12 < var19) {
            var19 = 1.0D - var12;
            var18 = 5;
         }

         float var21 = 0.1F;
         if(var18 == 0) {
            this.motionX = (double)(-var21);
         }

         if(var18 == 1) {
            this.motionX = (double)var21;
         }

         if(var18 == 4) {
            this.motionZ = (double)(-var21);
         }

         if(var18 == 5) {
            this.motionZ = (double)var21;
         }
      }

      return false;
   }
   
   @Override
   public boolean isEntityInsideOpaqueBlock() {
      if (canRunSPC() && ph.noClip) {
         return false;
      }
      return super.isEntityInsideOpaqueBlock();
   }   

   @Override
   protected String getHurtSound() {
      if (multiplayer || (canRunSPC() && ph.damage)) {
         return super.getHurtSound();
      } else {
         return "";
      }
   }

   @Override
   public float getCurrentPlayerStrVsBlock(Block block) {
      if (canRunSPC() && ph.instant) {
         return Float.MAX_VALUE;
      }
      return super.getCurrentPlayerStrVsBlock(block);
   }

   @Override
   public boolean canHarvestBlock(Block block) {
      if (canRunSPC() && ph.instant) {
         return true;
      }
      return super.canHarvestBlock(block);
   }

   @Override
   protected void fall(float f) {
      if (canRunSPC() && !ph.falldamage) {
         return;
      }
      super.fall(f);
   }

   @Override
   protected void jump() {
      if (canRunSPC() && ph.gravity > 1.0D) {
         this.motionY = (0.4199999868869782D * ph.gravity);
         return;
      }
      super.jump();
   }

   @Override
   public void moveFlying(float f, float f1, float f2) {
      if (!canRunSPC() || ph.speed <= 1.0F) {
         super.moveFlying(f, f1, f2);
         return;
      }
      float f3 = MathHelper.sqrt_float(f * f + f1 * f1);
      if (f3 < 0.01F) {
         return;
      }
      if (f3 < 1.0F) {
         f3 = 1.0F;
      }
      f3 = f2 / f3;
      f *= f3;
      f1 *= f3;
      float f4 = MathHelper.sin(this.rotationYaw * 3.141593F / 180.0F);
      float f5 = MathHelper.cos(this.rotationYaw * 3.141593F / 180.0F);
      double speed = ((canRunSPC()) ? ph.speed : 1);
      this.motionX += (f * f5 - f1 * f4) * speed;
      this.motionZ += (f1 * f5 + f * f4) * speed;
   }

   @Override
   public void onUpdate() {
      if (canRunSPC()) {
         ph.beforeUpdate();
         super.onUpdate();
         ph.afterUpdate();
      } else {
         super.onUpdate();
      }
   }

   @Override
   protected void damageEntity(int i) {
      if (canRunSPC() && !ph.damage) {
         return;
      }
      super.damageEntity(i);
   }

   @Override
   public void setEntityDead() {
      if (canRunSPC()) {
         ph.setCurrentPosition();
      }
      super.setEntityDead();
   }

   @Override
   public double getDistanceSqToEntity(Entity entity) {
      if (canRunSPC() && (!ph.mobdamage || ph.mobsfrozen)) {
         return Double.MAX_VALUE;
      }
      return super.getDistanceSqToEntity(entity);
   }

   @Override
   public void onDeath(Entity entity) {
      if (canRunSPC() && ph.keepitems && PlayerHelper.INV_BEFORE_DEATH != null) {

         for (int j = 0; j < inventory.armorInventory.length; j++) {
            PlayerHelper.INV_BEFORE_DEATH.armorInventory[j] = inventory.armorItemInSlot(j);
         }
         for (int j = 0; j < inventory.mainInventory.length; j++) {
            PlayerHelper.INV_BEFORE_DEATH.mainInventory[j] = inventory.mainInventory[j];
         }
         ph.destroyInventory();
      }
      super.onDeath(entity);
   }

   @Override
   public void attackTargetEntityWithCurrentItem(Entity entity) {
      if (canRunSPC() && ph.instantkill) {
         entity.attackEntityFrom(this, Integer.MAX_VALUE);
         entity.kill();
         return;
      } else if (canRunSPC() && ph.criticalHit) {
         double my = motionY;
         boolean og = onGround;
         boolean iw = inWater;
         float fd = fallDistance;
         super.motionY = -0.1D;
         super.inWater = false;
         super.onGround = false;
         super.fallDistance = 0.1F;
         super.attackTargetEntityWithCurrentItem(entity);
         motionY = my;
         onGround = og;
         inWater = iw;
         fallDistance = fd;
         return;
      }
      super.attackTargetEntityWithCurrentItem(entity);
   }

   @Override
   public boolean handleWaterMovement() {
      if (canRunSPC() && !ph.watermovement) {
         return false;
      }
      return super.handleWaterMovement();
   }

   @Override
   public boolean handleLavaMovement() {
      if (canRunSPC() && !ph.watermovement) {
         return false;
      }
      return super.handleLavaMovement();
   }

   @Override
   public void dropPlayerItemWithRandomChoice(ItemStack itemstack, boolean flag) {
      if (canRunSPC()) {
         ph.givePlayerItemNaturally(itemstack);
      }
      super.dropPlayerItemWithRandomChoice(itemstack,flag);
   }

   @Override
   public MovingObjectPosition rayTrace(double d, float f) {
      if (canRunSPC() && d == this.mc.playerController.getBlockReachDistance()) {
         d = ph.reachdistance;
      }
      return super.rayTrace(d, f);
   }
   
   @Override
   public boolean isOnLadder() {
      if (canRunSPC() && ph.ladderMode && isCollidedHorizontally) {
         return true;
      }
      return super.isOnLadder();
   }

   /*
    * showErrorFrame - shows a Swing JFrame containing troubleshooting information if SPC was installed incorrectly.
    */
   public void showErrorFrame() {
      JFrame frame = new JFrame();
      JTextPane textarea = new JTextPane();

      frame.setBackground(Color.lightGray);
      textarea.setContentType("text/html");

      String text = "<html><p>";
      text = text.concat("Single Player Commands v" + SPCVERSION.getVersion() + " for Minecraft version " + MCVERSION + "<br />");
      text = text.concat("Running Minecraft version " + curmcversion + "<br />");
      text = text.concat("You are missing the following class files necessary for <br />" + "Single Player Commands to operate:<br /><br /><ul>");
      for (String missing : missingRequiredClasses) {
         text = text.concat("<li>" + missing + "</li>");
      }
      text = text.concat("</ul><br />");

      text = text.concat("Make sure that all of the class files listed above are in minecraft.jar.<br />");
      text = text.concat("If they are not, copy them from your SPC download folder into minecraft.jar<br />" + "and try running Minecraft again.<br />");
      text = text.concat("If errors persist, copy and paste this error log to <a href=\"http://bit.ly/spcmod\">http://bit.ly/spcmod</a> for help.");
      text = text.concat("</p></html>");

      textarea.setText(text);

      frame.setLayout(new BorderLayout());
      frame.add(textarea, BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
      addChatMessage("\2474" + "SPC Error: Not installed properly.");
      addChatMessage("\2474" + "Check dialog window for more information.");
      MESSAGESHOWN = new Object();
   }

   /*
    * initPlayerHelper - initializes the PlayerHelper variable. Only called if all necessary SPC files exist.
    */
   public void initPlayerHelper(Session session) {
      ph = new PlayerHelper(this.mc, this);
      ph.readWaypointsFromNBT(ph.getWorldDir());
      multiplayer = mc.isMultiplayerWorld();

      if (STARTUP == null && !multiplayer) {
         ph.sendMessage("\2478Single Player Commands (" + SPCVERSION.getVersion() + ") - http://bit.ly/spcmod");
         Calendar cal = Calendar.getInstance();
         if (cal.get(Calendar.DAY_OF_MONTH) == 25 && cal.get(Calendar.MONTH) == 11) {
            String name = username == null || username.equalsIgnoreCase("") ? "" : "Dear " + username + ", ";
            ph.sendMessage("\2474" + name + "Merry Christmas! From simo_415");
         } else if (cal.get(Calendar.DAY_OF_MONTH) == 6 && cal.get(Calendar.MONTH) == 11) {
            ph.sendMessage("\2475Happy birthday Single Player Commands. Now a year older!");
         }
         STARTUP = new Object();
      }
      if (session != null && session.username != null && session.username.length() > 0) {
         ph.sessionusername = session.username;
      }
   }

   /*
    * checkClasses - checks if all the required SPC classes exist. If they do, returns true. Otherwise, returns false.
    */
   public boolean checkClasses() {
      missingRequiredClasses = new Vector<String>();
      missingOptionalClasses = new Vector<String>();
      phexists = true;
      curmcversion = Display.getTitle().split(" ")[Display.getTitle().split(" ").length - 1];
      /*
       * Pointless bit of code which trunks insists on leaving in
       */
      if (!curmcversion.equalsIgnoreCase(MCVERSION)) {
         addChatMessage("\2474" + "Single Player Commands v" + SPCVERSION.getVersion() + " is not compatible with Minecraft v" + curmcversion);
         addChatMessage("\2474" + "Visit http://bit.ly/spcmod to download the correct version.");
         System.err.println("Single Player Commands v" + SPCVERSION.getVersion() + " is not compatible with Minecraft v" + curmcversion);
         System.err.println("Visit http://bit.ly/spcmod to download the correct version.");
      }
      Package p = EntityPlayerSP.class.getPackage();
      String prefix = p == null ? "" : p.getName() + ".";
      String requiredClasses[] = new String[] { "PlayerHelper", "Settings", "SPCPlugin", "SPCPluginManager", "SPCCommand" };
      String optionalClasses[] = new String[] { "spc_WorldEdit", "SPCLocalConfiguration", "SPCLocalPlayer", "SPCLocalWorld", "SPCServerInterface", "WorldEditPlugin" };

      for (String classname : requiredClasses) {
         try {
            Class.forName(prefix + classname);
         } catch (Throwable e) {
            missingRequiredClasses.add(classname);
         }
      }
      for (String classname : optionalClasses) {
         try {
            Class.forName(prefix + classname);
         } catch (Throwable e) {
            missingOptionalClasses.add(classname);
         }
      }

      if (missingRequiredClasses.size() != 0) {
         addChatMessage("\2474" + "You are missing these class files: ");
         String list = "";
         for (String missing : missingRequiredClasses) {
            list += missing + ", ";
         }
         addChatMessage("\2474" + list);
         addChatMessage("\2474" + "Please try reinstalling.");
         phexists = false;
      }
      return phexists;
   }

   /**
    * Checks if SPC is allowed to run or not.
    * @return true if SPC is allowed to run
    */
   public boolean canRunSPC() {
      return phexists && !multiplayer;
   }
}
