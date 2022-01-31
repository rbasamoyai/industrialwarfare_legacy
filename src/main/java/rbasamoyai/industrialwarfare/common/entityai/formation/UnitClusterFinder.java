package rbasamoyai.industrialwarfare.common.entityai.formation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;

import net.minecraft.entity.CreatureEntity;

public class UnitClusterFinder {

	public static final int NOISE = -1;
	
	private final int minUnits;
	private final double epsilion;
	private final Map<CreatureEntity, Integer> labels = new HashMap<>();

	public UnitClusterFinder(int minUnits, double epsilion) {
		this.minUnits = minUnits;
		this.epsilion = epsilion;
	}
	
	public List<List<CreatureEntity>> findClusters(List<CreatureEntity> units) {
		int count = 0;
		for (CreatureEntity unit : units) {
			if (this.labels.containsKey(unit)) continue;
			List<CreatureEntity> neighbors = this.rangeQuery(units, unit);
			if (neighbors.size() < this.minUnits) {
				this.labels.put(unit, NOISE);
				continue;
			}
			this.labels.put(unit, count);
			Queue<CreatureEntity> unitsToCheck = new LinkedList<>();
			unitsToCheck.addAll(neighbors);
			while (!unitsToCheck.isEmpty()) {
				CreatureEntity neighbor = unitsToCheck.remove();
				if (unit == neighbor) continue;
				if (this.labels.containsKey(neighbor)) {
					if (this.labels.get(neighbor) == NOISE) this.labels.put(neighbor, count);
					continue;
				}		
				this.labels.put(neighbor, count);
				List<CreatureEntity> neighborsOfNeighbor = this.rangeQuery(units, neighbor);
				if (neighborsOfNeighbor.size() >= this.minUnits) unitsToCheck.addAll(neighborsOfNeighbor);
			}
			++count;
		}
		return this.condenseLabels();
	}
	
	private List<CreatureEntity> rangeQuery(List<CreatureEntity> units, CreatureEntity unit) {
		List<CreatureEntity> neighbors = new ArrayList<>();
		for (CreatureEntity unit1 : units) {
			if (unit.position().closerThan(unit1.position(), this.epsilion))
				neighbors.add(unit1);
		}
		return neighbors;
	}
	
	private List<List<CreatureEntity>> condenseLabels() {
		Map<Integer, List<CreatureEntity>> numberedClusters = new HashMap<>();
		List<CreatureEntity> noise = new ArrayList<>();
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
		List<Entry<Integer, List<CreatureEntity>>> unorderedClusters = new ArrayList<>();
		unorderedClusters.addAll(numberedClusters.entrySet());
		unorderedClusters.sort((a, b) -> Integer.compare(a.getKey(), b.getKey()));
		List<List<CreatureEntity>> orderedClusters =
				unorderedClusters
				.stream()
				.map(Entry::getValue)
				.collect(Collectors.toList());
		orderedClusters.add(noise);
		return orderedClusters;
	}
	
}