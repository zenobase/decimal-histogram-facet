package com.zenobase.search.facet.decimalhistogram;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.facet.FacetModule;

public class DecimalHistogramFacetPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "decimal-histogram-facet";
	}

	@Override
	public String description() {
		return "Facet for decimal histograms";
	}

	public void onModule(FacetModule module) {
		module.addFacetProcessor(DecimalHistogramFacetParser.class);
		InternalDecimalHistogramFacet.registerStreams();
	}
}
