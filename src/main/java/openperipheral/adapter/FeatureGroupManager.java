package openperipheral.adapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import openperipheral.api.architecture.IFeatureGroupManager;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class FeatureGroupManager implements IFeatureGroupManager {

	public static final FeatureGroupManager INSTANCE = new FeatureGroupManager();

	private static class FeatureGroupProperties {
		private final Set<String> blacklistedArchitectures = Sets.newHashSet();

		public void disable(String architecture) {
			blacklistedArchitectures.add(architecture);
		}

		public void enable(String architecture) {
			blacklistedArchitectures.remove(architecture);
		}

		public boolean isEnabled(String architecture) {
			return !blacklistedArchitectures.contains(architecture);
		}
	}

	private final Map<String, FeatureGroupProperties> featureGroups = Maps.newHashMap();

	private FeatureGroupProperties getOrCreate(String featureGroup) {
		FeatureGroupProperties result = featureGroups.get(featureGroup);
		if (result == null) {
			result = new FeatureGroupProperties();
			featureGroups.put(featureGroup, result);
		}

		return result;
	}

	public void ensureExists(String featureGroup) {
		if (!featureGroups.containsKey(featureGroup)) featureGroups.put(featureGroup, new FeatureGroupProperties());
	}

	@Override
	public Set<String> knownFeatureGroups() {
		return Collections.unmodifiableSet(featureGroups.keySet());
	}

	@Override
	public void disable(String featureGroup, String architecture) {
		getOrCreate(featureGroup).disable(architecture);
	}

	@Override
	public void enable(String featureGroup, String architecture) {
		getOrCreate(featureGroup).enable(architecture);
	}

	@Override
	public boolean isEnabled(String featureGroup, String architecture) {
		return getOrCreate(featureGroup).isEnabled(architecture);
	}

}