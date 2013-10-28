package com.zenobase.search.facet.decimalhistogram;

import java.util.List;

import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.histogram.HistogramFacet;

public interface DecimalHistogramFacet extends Facet {

	/**
	 * The type of the filter facet.
	 */
	public String TYPE = "decimal_histogram";

	/**
	 * An ordered list of decimal histogram facet entries.
	 */
	List<? extends Entry> getEntries();

	interface Entry extends HistogramFacet.Entry {

		double getKey(double interval);
	}
}
