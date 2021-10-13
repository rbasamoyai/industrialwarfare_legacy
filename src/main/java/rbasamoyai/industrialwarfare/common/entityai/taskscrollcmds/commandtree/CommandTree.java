package rbasamoyai.industrialwarfare.common.entityai.taskscrollcmds.commandtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import rbasamoyai.industrialwarfare.common.items.taskscroll.ArgWrapper;
import rbasamoyai.industrialwarfare.common.items.taskscroll.IArgHolder;

public class CommandTree {

	private final Node startNode;
	
	private CommandTree(Node startNode) {
		this.startNode = startNode;
	}
	
	public List<IArgHolder> fillArgumentsAfterPoint(List<IArgHolder> oldArgs, int argIndex, ArgWrapper afterWrapper) {
		return this.iterateTree(i -> {
			return i <= argIndex && 0 <= i && i < oldArgs.size() ? oldArgs.get(i).getWrapper() : afterWrapper;
		});
	}
	
	public List<IArgHolder> getArguments(List<ArgWrapper> wrapperList) {
		return this.iterateTree(i -> {
			return i < wrapperList.size() ? wrapperList.get(i) : ArgWrapper.EMPTY;
		});
	}
	
	public List<IArgHolder> getArgumentsWith(ArgWrapper wrapper) {
		return this.iterateTree(i -> {
			return wrapper;
		});
	}
	
	private List<IArgHolder> iterateTree(Function<Integer, ArgWrapper> wrapperGetter) {
		List<IArgHolder> args = new ArrayList<>();
		Optional<Node> optional = Optional.of(this.startNode);
		
		int i = 0;
		while (optional.isPresent()) {
			Node node = optional.get();
			ArgWrapper wrapper = wrapperGetter.apply(i++);
			IArgHolder holder = node.getNewHolder();
			holder.accept(wrapper);
			args.add(holder);
			optional = node.findMatchingChild(wrapper);
		}
		
		return args;
	}
	
	public boolean differentBranches(List<IArgHolder> args, ArgWrapper a, ArgWrapper b, int index) {
		if (args.size() != index)
			throw new IllegalArgumentException("args#size must be equal to index");
		
		Optional<Node> optional = Optional.of(this.startNode);
		int i = 0;
		while (optional.isPresent() && i < index) {
			Node node = optional.get();
			ArgWrapper wrapper = args.get(i++).getWrapper();
			optional = node.findMatchingChild(wrapper);
		}
		if (!optional.isPresent() && i != index) {
			return false;
		} else {
			Node node = optional.get();
			int aMatches = -1;
			int bMatches = -1;
			for (int c = 0; c < node.children.size(); c++) { // is this a motherfucking c++ reference
				Predicate<ArgWrapper> pred = node.children.get(c).getFirst();
				if (pred.test(a)) aMatches = c;
				if (pred.test(b)) bMatches = c;
			}
			return aMatches != bMatches;
		}
	}
	
	public IArgHolder getInitialHolder() {
		return this.startNode.getNewHolder();
	}
	
	public static Builder builder(Supplier<IArgHolder> headSupplier) {
		return new Builder(headSupplier);
	}
	
	@Override
	public String toString() {
		return this.startNode.toString();
	}
	
	private static class Node {
		private final Supplier<IArgHolder> supplier;
		private final List<Pair<Predicate<ArgWrapper>, Node>> children;
		
		public Node(Supplier<IArgHolder> supplier, List<Pair<Predicate<ArgWrapper>, Node>> children) {
			this.supplier = supplier;
			this.children = children;
		}
		
		public IArgHolder getNewHolder() {
			return this.supplier.get();
		}
		
		public Optional<Node> findMatchingChild(ArgWrapper parentWrapper) {
			return this.children.stream()
					.filter(child -> child.getFirst().test(parentWrapper))
					.findFirst()
					.map(child -> Optional.ofNullable(child.getSecond()))
					.orElse(Optional.empty());
		}
		
		@Override
		public String toString() {
			return "CommandTree$Node[supplier=" + this.supplier.toString() + ", childrenCount=" + this.children.size() + "]"; 
		}
	}
	
	public static class Builder {
		private static final List<Pair<Predicate<ArgWrapper>, Node>> EMPTY_LIST = new ArrayList<>();
		
		private Stack<StackNode> stack = new Stack<>();
		private Supplier<IArgHolder> workingSupplier;
		private Predicate<ArgWrapper> workingPredicate;
		private List<Pair<Predicate<ArgWrapper>, Node>> workingChildren;
		
		public Builder(Supplier<IArgHolder> headSupplier) {
			this.workingSupplier = headSupplier;
			this.workingPredicate = wrapper -> true; // Head of CommandTree doesn't need a predicate
			this.workingChildren = new ArrayList<>();
		}
		
		public Builder beginNode(Supplier<IArgHolder> workingSupplier, Predicate<ArgWrapper> workingPredicate) {
			stack.push(new StackNode(this));
			this.workingSupplier = workingSupplier;
			this.workingPredicate = workingPredicate;
			this.workingChildren = new ArrayList<>();
			return this;
		}
		
		public Builder endNode() {
			Pair<Predicate<ArgWrapper>, Node> finishedChild = Pair.of(this.workingPredicate, new Node(this.workingSupplier, this.workingChildren));
			stack.pop().restore(this);
			this.workingChildren.add(finishedChild);
			return this;
		}
		
		public Builder addTerminalNode(Supplier<IArgHolder> supplier, Predicate<ArgWrapper> predicate) {
			this.workingChildren.add(Pair.of(predicate, new Node(supplier, EMPTY_LIST)));
			return this;
		}
		
		public CommandTree build() {
			if (this.stack.size() > 0)
				throw new IllegalStateException("Cannot build this CommandTree - there are still " + this.stack.size() + " node layers that haven't been ended yet");
			return new CommandTree(new Node(this.workingSupplier, this.workingChildren));
		}
		
		private static class StackNode {
			private final Supplier<IArgHolder> heldSupplier;
			private final Predicate<ArgWrapper> heldPredicate;
			private final List<Pair<Predicate<ArgWrapper>, Node>> heldChildren;
			
			public StackNode(Builder builder) {
				this.heldSupplier = builder.workingSupplier;
				this.heldPredicate = builder.workingPredicate;
				this.heldChildren = builder.workingChildren;
			}
			
			public void restore(Builder builder) {
				builder.workingSupplier = this.heldSupplier;
				builder.workingPredicate = this.heldPredicate;
				builder.workingChildren = this.heldChildren;
			}
		}
		
	}
	
}
