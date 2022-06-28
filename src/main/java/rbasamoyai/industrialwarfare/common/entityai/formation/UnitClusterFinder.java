package rbasamoyai.industrialwarfare.common.entityai.formation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;

import net.minecraft.world.entity.PathfinderMob;

public class UnitClusterFinder {

	public static final int NOISE = -1;
	
	private final int minUnits;
	private final double epsilion;
	private final Map<PathfinderMob, Integer> labels = new HashMap<>();

	public UnitClusterFinder(int minUnits, double epsilion) {
		this.minUnits = minUnits;
		this.epsilion = epsilion;
	}
	
	public List<List<PathfinderMob>> findClusters(List<PathfinderMob> units) {
		int count = 0;
		for (PathfinderMob unit : units) {
			if (this.labels.containsKey(unit)) continue;
			List<PathfinderMob> neighbors = this.rangeQuery(units, unit);
			if (neighbors.size() < this.minUnits) {
				this.labels.put(unit, NOISE);
				continue;
			}
			this.labels.put(unit, count);
			Queue<PathfinderMob> unitsToCheck = new LinkedList<>();
			unitsToCheck.addAll(neighbors);
			while (!unitsToCheck.isEmpty()) {
				PathfinderMob neighbor = unitsToCheck.remove();
				if (unit == neighbor) continue;
				if (this.labels.containsKey(neighbor)) {
					if (this.labels.get(neighbor) == NOISE) this.labels.put(neighbor, count);
					continue;
				}		
				this.labels.put(neighbor, count);
				List<PathfinderMob> neighborsOfNeighbor = this.rangeQuery(units, neighbor);
				if (neighborsOfNeighbor.size() >= this.minUnits) unitsToCheck.addAll(neighborsOfNeighbor);
			}
			++count;
		}
		return this.condenseLabels();
	}
	
	private List<PathfinderMob> rangeQuery(List<PathfinderMob> units, PathfinderMob unit) {
		List<PathfinderMob> neighbors = new ArrayList<>();
		for (PathfinderMob unit1 : units) {
			if (unit.position().closerThan(unit1.position(), this.epsilion))
				neighbors.add(unit1);
		}
		return neighbors;
	}
	
	private List<List<PathfinderMob>> condenseLabels() {
		Map<Integer, List<PathfinderMob>> numberedClusters = new HashMap<>();
		List<PathfinderMob> noise = new ArrayList<>();
		this.labels.forEach((unit, n) -> {
			int n1 = n.intValue();
			if (n1 == NOISE) {
				noise.add(unit);
				return;
			}
			if (!numberedClusters.containsKey(n1))
				numberedClusters.put(n1, new ArrayList<>());
			numberedClusters.get(n1).add(unit);
		});
		List<Entry<Integer, List<PathfinderMob>>> unorderedClusters = new ArrayList<>();
		unorderedClusters.addAll(numberedClusters.entrySet());
		unorderedClusters.sort((a, b) -> Integer.compare(a.getKey(), b.getKey()));
		List<List<PathfinderMob>> orderedClusters =
				unorderedClusters
				.stream()
				.map(Entry::getValue)
				.collect(Collectors.toList());
		orderedClusters.add(noise);
		return orderedClusters;
	}
	
}