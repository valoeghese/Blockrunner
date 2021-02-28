/*
 * Lint
 * Copyright (C) 2020 hYdos, Valoeghese, ramidzkh
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package valoeghese.blockrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class BlockRunner {
	private BlockRunner(List<Entry> values) {
		this.values = values;
	}

	private List<Entry> values;

	public static class Builder {
		public Builder() {
			this.positions = ImmutableList.of(new Entry(BlockPos.ORIGIN, Blocks.STONE.getDefaultState())).stream();
		}

		private Builder(Stream<Entry> positions) {
			this.positions = positions;
		}

		private Stream<Entry> positions; 

		private Builder append(Set<Entry> entries) {
			this.positions = Stream.concat(this.positions, entries.stream());
			return this;
		}

		public Builder map(Function<BlockPos, BlockPos> mappingFunction) {
			this.positions = this.positions.map(entry -> new Entry(mappingFunction.apply(entry.pos), entry.state));
			return this;
		}

		public Builder texture(BiFunction<BlockPos, BlockState, BlockState> mappingFunction) {
			this.positions = this.positions.map(entry -> new Entry(entry.pos, mappingFunction.apply(entry.pos, entry.state)));
			return this;
		}

		public Builder expand(Direction direction, int amount) {
			this.positions = this.positions.flatMap(entry -> {
				ArrayList<Entry> list = new ArrayList<>();

				for (int i = 0; i < amount; ++i) {
					list.add(new Entry(entry.pos.up(), entry.state));
				}

				return list.stream();
			});

			return this;
		}

		public Builder when(Predicate<BlockPos> condition, Function<Builder, Builder> callback) {
			Set<Entry> entries = this.positions.filter(entry -> !condition.test(entry.pos)).collect(Collectors.toSet());
			return callback.apply(new Builder(this.positions.filter(entry -> condition.test(entry.pos)))).append(entries);
		}

		public BlockRunner build() {
			return new BlockRunner(this.positions.collect(Collectors.toList()));
		}
	}

	public void generate(WorldAccess world, BlockPos pos, int flags) {
		for (Entry entry : this.values) {
			world.setBlockState(pos.add(entry.pos), entry.state, flags);
		}
	}

	public void generate(WorldAccess world, BlockPos pos) {
		this.generate(world, pos, 3);
	}
}

class Entry {
	Entry(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}

	final BlockPos pos;
	final BlockState state;
}

