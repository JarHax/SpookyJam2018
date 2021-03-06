package com.jarhax.eerieentities.entities;

import java.util.UUID;

import javax.annotation.Nullable;

import com.jarhax.eerieentities.EerieEntities;
import com.jarhax.eerieentities.config.Config;

import net.darkhax.bookshelf.data.AttributeOperation;
import net.darkhax.bookshelf.lib.Constants;
import net.darkhax.bookshelf.util.MathsUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityNetherKnight extends EntityMob {
    
    private static final DataParameter<Integer> RUNE_WORD = EntityDataManager.<Integer> createKey(EntityNetherKnight.class, DataSerializers.VARINT);
    public static final char[][] WORDS = { { 68, 65, 82, 75 }, { 70, 73, 82, 69 }, { 71, 69, 71, 89 }, { 83, 65, 76, 84 }, { 67, 85, 78, 84 } };
    
    private static final AttributeModifier BUFF_ARMOR = new AttributeModifier(UUID.fromString("cb1a4e88-69d3-4ba4-a6de-ea98bc63114f"), "knight_buff_armor", Config.netherKnight.getBonusArmor(), AttributeOperation.ADDITIVE.ordinal());
    private static final AttributeModifier BUFF_HEALTH = new AttributeModifier(UUID.fromString("df154adf-523a-4523-bf02-08115e8a666f"), "knight_buff_health", Config.netherKnight.getBonusHealth(), AttributeOperation.ADDITIVE.ordinal());
    
    public EntityNetherKnight(World world) {
        
        super(world);
        this.isImmuneToFire = true;
    }
    
    public int getRuneWord () {
        
        return this.dataManager.get(RUNE_WORD).intValue();
    }
    
    public void setRuneWord (int value) {
        
        this.dataManager.set(RUNE_WORD, value);
    }
    
    public char getRune (int index) {
        
        return index >= 0 && index < 4 ? WORDS[this.getRuneWord()][index] : 'X';
    }
    
    @Override
    public void entityInit () {
        
        super.entityInit();
        this.dataManager.register(RUNE_WORD, 0);
    }
    
    @Override
    public IEntityLivingData onInitialSpawn (DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setRuneWord(Constants.RANDOM.nextInt(WORDS.length));
        return livingdata;
    }
    
    @Override
    public void writeEntityToNBT (NBTTagCompound compound) {
        
        super.writeEntityToNBT(compound);
        compound.setInteger("RuneWord", this.getRuneWord());
    }
    
    @Override
    public void readEntityFromNBT (NBTTagCompound compound) {
        
        super.readEntityFromNBT(compound);
        this.setRuneWord(compound.getInteger("RuneWord"));
    }
    
    @Override
    public float getEyeHeight () {
        
        return this.height * 1.15F;
    }
    
    @Override
    public int getMaxSpawnedInChunk () {
        
        return Config.netherKnight.getMaxInChunk();
    }
    
    @Override
    public void applyEntityAttributes () {
        
        super.applyEntityAttributes();
        Config.netherKnight.apply(this);
        this.experienceValue = Config.netherKnight.getBaseEXP();
    }
    
    @Override
    protected void initEntityAI () {
        
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }
    
    @Override
    protected SoundEvent getAmbientSound () {
        
        return SoundEvents.ENTITY_BLAZE_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound (DamageSource damageSourceIn) {
        
        return SoundEvents.ENTITY_BLAZE_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound () {
        
        return SoundEvents.ENTITY_BLAZE_DEATH;
    }
    
    @Override
    public void onLivingUpdate () {
        
        if (this.world.isRemote) {
            
            if (this.rand.nextInt(24) == 0 && !this.isSilent()) {
                
                this.world.playSound(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, SoundEvents.ENTITY_BLAZE_BURN, this.getSoundCategory(), 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F, false);
            }
            
            for (int i = 0; i < 2; ++i) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - 0.5D) * this.width, this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width, 0.0D, 0.0D, 0.0D);
            }
        }
        
        super.onLivingUpdate();
    }
    
    @Override
    protected void updateAITasks () {
        
        if (this.isWet()) {
            
            this.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 25));
        }
    }
    
    @Override
    public ResourceLocation getLootTable () {
        
        return EerieEntities.LOOT_NETHER_KNIGHT;
    }
    
    @Override
    public void fall (float distance, float damageMultiplier) {
        
        // no fall damage
    }
    
    @Override
    protected boolean isValidLightLevel () {
        
        return true;
    }
    
    @Override
    public boolean attackEntityFrom (DamageSource source, float amount) {
        
        final Entity sourceEnt = source.getImmediateSource();
        
        if (!source.isUnblockable() && (source.isMagicDamage() || source.isProjectile() || source.isExplosion())) {
            
            amount /= 2f;
        }
        
        if (sourceEnt != null && MathsUtils.tryPercentage(Config.netherKnight.getBurnChance())) {
            
            sourceEnt.setFire(1);
        }
        
        if (super.attackEntityFrom(source, amount)) {
            
            EntityLivingBase target = this.getAttackTarget();
            
            // If the mob isn't targeting anyone, try to target the attacker.
            if (target == null && source.getTrueSource() instanceof EntityLivingBase) {
                
                target = (EntityLivingBase) source.getTrueSource();
            }
            
            // If conditions are right, try to spawn reinforcements
            if (target != null && MathsUtils.tryPercentage(Config.netherKnight.getReinforcementChance())) {
                
                for (int attempt = 0; attempt < 25; attempt++) {
                    
                    final int[] range = Config.netherKnight.getSpawnRange();
                    final int spawnOffsetX = MathHelper.getInt(this.rand, range[0], range[1]) * MathHelper.getInt(this.rand, -1, 1);
                    final int spawnOffsetZ = MathHelper.getInt(this.rand, range[0], range[1]) * MathHelper.getInt(this.rand, -1, 1);
                    final BlockPos spawnPos = this.getPosition().add(spawnOffsetX, -1, spawnOffsetZ);
                    final IBlockState state = this.world.getBlockState(spawnPos);
                    
                    if (state != null && state.isSideSolid(this.world, spawnPos, EnumFacing.UP) && this.world.isAirBlock(spawnPos.up(2))) {
                        
                        try {
                            
                            final EntityLiving reinforcement = (EntityLiving) EntityList.createEntityByIDFromName(Config.netherKnight.getReinforcementIDs()[Constants.RANDOM.nextInt(Config.netherKnight.getReinforcementIDs().length)], this.world);
                            reinforcement.setPositionAndUpdate(spawnPos.getX() + 0.5f, this.posY, spawnPos.getZ() + 0.5f);
                            reinforcement.setAttackTarget(target);
                            this.world.spawnEntity(reinforcement);
                            reinforcement.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(BUFF_ARMOR);
                            reinforcement.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(BUFF_HEALTH);
                            reinforcement.setHealth(reinforcement.getMaxHealth());
                        }
                        
                        catch (final Exception e) {
                            
                            EerieEntities.LOG.catching(e);
                        }
                        
                        break;
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }
}