package core;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import utils.DistributionUnit;
import utils.UtilityMetrics;

/**
 * This class stores all the information about a discriminating view and performs computations
 * on the view. Specifically, it stores the groupby and aggregate attributes, distributions for
 * the query and full dataset and utility. It has functions to compute utility based on various 
 * metrics
 * 
 * @author manasi
 * 
 */
public class DiscriminatingView {
	private List<String> aggregateAttributes;
	private List<String> groupByAttributes;
	private List<DistributionUnit> queryDistribution;
	private List<DistributionUnit> datasetDistribution;
	private Map<String, double[]> combinedDistribution;
	private List<String> combinedDistributionAsStrings;
	
	private double viewUtility;
	
	private DiscriminatingView() {
		this.aggregateAttributes = Lists.newArrayList();
		this.groupByAttributes = Lists.newArrayList();
	}
	
	private void fixDistributions() {
		// make both distributions uniform (i.e. same values)
		combinedDistribution = new Hashtable<String, double[]>();
		
		// populate the distributions for the entire dataset, placeholder for query distribution
		for (DistributionUnit d: datasetDistribution) {
			combinedDistribution.put(d.attributeValue.toString(), new double[]{d.fraction, 0});	
		}
		
		// update query distribution
		for (DistributionUnit d: queryDistribution) {
			if (!combinedDistribution.containsKey(d.attributeValue)) {
				combinedDistribution.put(d.attributeValue.toString(), new double[]{0, d.fraction});
			} else {
				combinedDistribution.get(d.attributeValue.toString())[1] = d.fraction;	
			}
		}
		
		datasetDistribution.clear();
		queryDistribution.clear();
		
		List<String> result = Lists.newArrayList();
		Collection<String> keys = this.combinedDistribution.keySet();
		for(String key : keys) {
			result.add(key + ":" + this.combinedDistribution.get(key)[0] + ":" + 
					this.combinedDistribution.get(key)[1]);
			// repopulate because some keys may have been missing from one or either distributions
			datasetDistribution.add(new DistributionUnit(key, combinedDistribution.get(key)[0]));
			queryDistribution.add(new DistributionUnit(key, combinedDistribution.get(key)[1]));
		}
		this.combinedDistributionAsStrings = result;		

	}
	
	public DiscriminatingView(List<String> aggregateAttributes, List<String> groupByAttributes, 
			List<DistributionUnit> queryDistribution, List<DistributionUnit> datasetDistribution) {
		this();
		this.aggregateAttributes.addAll(aggregateAttributes);
		this.groupByAttributes.addAll(groupByAttributes);	
		this.queryDistribution = queryDistribution;
		this.datasetDistribution = datasetDistribution;
		viewUtility = 0;
	}
	
	public DiscriminatingView(String aggregateAttribute, String groupByAttribute, 
			List<DistributionUnit> queryDistribution, List<DistributionUnit> datasetDistribution)
	{
		this();
		this.aggregateAttributes.add(aggregateAttribute);
		this.groupByAttributes.add(groupByAttribute);	
		this.queryDistribution = queryDistribution;
		this.datasetDistribution = datasetDistribution;
		viewUtility = 0;
		
	}
	
	public List<DistributionUnit> getQueryDistribution() {
		return this.queryDistribution;
	}
	
	public List<DistributionUnit> getDatasetDistribution() {
		return this.datasetDistribution;
	}
	
	/**
	 * This function returns a list of strings which has the attributevalue followed by fraction
	 * in entire dataset and fraction in query. Example return value:
	 * ["ABC:0.5:0.01", "DEF:0.33:0.21"]
	 * 
	 */
	public List<String> getCombinedDistribution() {
		// format as strings
		return combinedDistributionAsStrings;
	}
	
	/**
	 * Computes the utility of this view based on the metric specified
	 * @param metric String name of metric to be used
	 */
	public void computeUtility(String metric)
	{
		if (metric.equals("EarthMoverDistance")) {
			this.viewUtility = UtilityMetrics.EarthMoverDistance(queryDistribution, datasetDistribution);
		} else if (metric.equals("EuclideanDistance")) {
			this.viewUtility = UtilityMetrics.EuclideanDistance(queryDistribution, datasetDistribution);
		} else if (metric.equals("CosineDistance")) {
			this.viewUtility = UtilityMetrics.CosineDistance(queryDistribution, datasetDistribution);
		} else if (metric.equals("FidelityDistance")) {
			this.viewUtility = UtilityMetrics.FidelityDistance(queryDistribution, datasetDistribution);
		} else if (metric.equals("ChiSquaredDistance")) {
			this.viewUtility = UtilityMetrics.ChiSquaredDistance(queryDistribution, datasetDistribution);
		} else if (metric.equals("EntropyDistance")) {
			this.viewUtility = UtilityMetrics.EntropyDistance(queryDistribution, datasetDistribution);
		}
	}
	
	public double getUtility()
	{
		return viewUtility;
	}
	
	public List<String> getAggregateAttribute()
	{
		return aggregateAttributes;
	}
	
	public List<String> getGroupByAttribute()
	{
		return groupByAttributes;
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o.getClass() != this.getClass()) return false;
		DiscriminatingView v = (DiscriminatingView) o;
		return v.toString().equalsIgnoreCase(this.toString());
	}
	
	public String toString() {
		return Joiner.on("-").join(this.aggregateAttributes) + "--" + Joiner.on("-").join(this.groupByAttributes);
	}
}
