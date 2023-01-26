package com.zacharybarbanell.coppercomponents;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrateBlock extends Block implements SimpleWaterloggedBlock {
   private static final Direction[] DIRECTIONS = Direction.values();
   public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
   public static final BooleanProperty EAST = BlockStateProperties.EAST;
   public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
   public static final BooleanProperty WEST = BlockStateProperties.WEST;
   public static final BooleanProperty UP = BlockStateProperties.UP;
   public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap
         .copyOf(Util.make(Maps.newEnumMap(Direction.class), (p_55164_) -> {
            p_55164_.put(Direction.NORTH, NORTH);
            p_55164_.put(Direction.EAST, EAST);
            p_55164_.put(Direction.SOUTH, SOUTH);
            p_55164_.put(Direction.WEST, WEST);
            p_55164_.put(Direction.UP, UP);
            p_55164_.put(Direction.DOWN, DOWN);
         }));
   private static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   private static final VoxelShape UP_AABB = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape CORE = Shapes.or(
      Block.box(0.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D),
      Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D),
      Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 16.0D)
   );
   public static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = ImmutableMap
         .copyOf(Util.make(Maps.newEnumMap(Direction.class), (p_55164_) -> {
            p_55164_.put(Direction.NORTH, NORTH_AABB);
            p_55164_.put(Direction.EAST, EAST_AABB);
            p_55164_.put(Direction.SOUTH, SOUTH_AABB);
            p_55164_.put(Direction.WEST, WEST_AABB);
            p_55164_.put(Direction.UP, UP_AABB);
            p_55164_.put(Direction.DOWN, DOWN_AABB);
         }));
   private final Map<BlockState, VoxelShape> shapesCache;

   public GrateBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(
            this.stateDefinition.any()
                  .setValue(DOWN, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false))
                  .setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false))
                  .setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false))
                  .setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream()
            .collect(Collectors.toMap(Function.identity(), GrateBlock::calculateShape)));
   }

   private static VoxelShape calculateShape(BlockState state) {
      VoxelShape voxelshape = Shapes.empty();
      for (Direction direction : DIRECTIONS) {
         if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
            voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
         }
      }
      if (state.getValue(POWERED)) {
         voxelshape = Shapes.join(voxelshape, CORE, BooleanOp.ONLY_FIRST);
      }
      return voxelshape;
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
      return this.shapesCache.get(state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
      if (state.getValue(POWERED)) {
         return Shapes.empty();
      }
      if (ctx instanceof EntityCollisionContext eCtx) {
         if (eCtx.getEntity() == null) {
            return this.getShape(state, getter, pos, ctx);
         }
         VoxelShape voxelshape = Shapes.empty();
         for (Direction direction : DIRECTIONS) {
            int threshold = pos.get(direction.getAxis())
                  + (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0);
            boolean result = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE
                  ? eCtx.getEntity().getBoundingBox().min(direction.getAxis()) >= threshold
                  : eCtx.getEntity().getBoundingBox().max(direction.getAxis()) <= threshold;
            if (result && state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
               voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
            }
         }
         return voxelshape;
      } else {
         return this.getShape(state, getter, pos, ctx);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
      if (blockstate.is(this)) {
         return blockstate.setValue(PROPERTY_BY_DIRECTION.get(ctx.getClickedFace()), true);
      } else {
         FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
         return super.getStateForPlacement(ctx).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER))
               .setValue(PROPERTY_BY_DIRECTION.get(ctx.getClickedFace()), true)
               .setValue(POWERED, Boolean.valueOf(ctx.getLevel().hasNeighborSignal(ctx.getClickedPos())));
      }
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
      return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
            state.getValue(PROPERTY_BY_DIRECTION.get(ctx.getClickedFace())) == false
                  ? true
                  : super.canBeReplaced(state, ctx);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED, POWERED);
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         boolean flag = pLevel.hasNeighborSignal(pPos);
         if (flag != pState.getValue(POWERED)) {
            if (pState.getValue(POWERED)) {
               pLevel.levelEvent(null, 1037, pPos, 0);
            }
            else {
               pLevel.levelEvent(null, 1037, pPos, 0);
            }

            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 2);
            if (pState.getValue(WATERLOGGED)) {
               pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }
         }

      }
   }
}
