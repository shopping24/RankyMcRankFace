/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.metric;

import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.utilities.SimpleMath;

public class DCGScorer extends MetricScorer {
	
	protected static double[] discount = null;//cache

	public DCGScorer()
	{
		this.k = 10;
		//init cache if we haven't already done so
		if(discount == null)
		{
			discount = new double[5000];
			for(int i=0;i<discount.length;i++)
				discount[i] = 1.0/SimpleMath.logBase2(i+2);
		}
	}
	public DCGScorer(int k)
	{
		this.k = k;
		//init cache if we haven't already done so
		if(discount == null)
		{
			discount = new double[5000];
			for(int i=0;i<discount.length;i++)
				discount[i] = 1.0/SimpleMath.logBase2(i+2);
		}
	}
	public MetricScorer copy()
	{
		return new DCGScorer();
	}
	/**
	 * Compute DCG at k. 
	 */
	public double score(RankList rl)
	{
		if(rl.size() == 0)
			return 0;

		int size = k;
		if(k > rl.size() || k <= 0)
			size = rl.size();
		
		float[] rel = getRelevanceLabels(rl);
		return getDCG(rel, size);
	}
	public double[][] swapChange(RankList rl)
	{
		float[] rel = getRelevanceLabels(rl);
		int size = (rl.size() > k) ? k : rl.size();
		double[][] changes = new double[rl.size()][];
		for(int i=0;i<rl.size();i++)
			changes[i] = new double[rl.size()];
		
		//for(int i=0;i<rl.size()-1;i++)//ignore K, compute changes from the entire ranked list
		for(int i=0;i<size;i++)
			for(int j=i+1;j<rl.size();j++)
				changes[j][i] = changes[i][j] = (discount(i) - discount(j)) * (rel[i] - rel[j]);

		return changes;
	}
	public String name()
	{
		return "DCG@"+k;
	}
	
	protected double getDCG(float[] rel, int topK)
	{
		double dcg = 0;
		for(int i=0;i<topK;i++)
			dcg += rel[i] * discount(i);
		return dcg;
	}
	
	//lazy caching
	protected double discount(int index)
	{
		if(index < discount.length)
			return discount[index];
		
		//we need to expand our cache
		int cacheSize = discount.length + 1000;
		while(cacheSize <= index)
			cacheSize += 1000;
		double[] tmp = new double[cacheSize];
		System.arraycopy(discount, 0, tmp, 0, discount.length);
		for(int i=discount.length;i<tmp.length;i++)
			tmp[i] = 1.0/SimpleMath.logBase2(i+2);
		discount = tmp;
		return discount[index];
	}

	protected float[] getRelevanceLabels(RankList rl)
	{
		float[] rel = new float[rl.size()];
		for(int i=0;i<rl.size();i++)
			rel[i] = rl.get(i).getLabel();
		return rel;
	}
}
